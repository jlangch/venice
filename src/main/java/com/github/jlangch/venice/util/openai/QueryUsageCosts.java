/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.util.openai;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.openai.client.OpenAIClient;
import com.openai.models.admin.organization.usage.UsageCostsParams;
import com.openai.models.admin.organization.usage.UsageCostsResponse;
import com.openai.models.admin.organization.usage.UsageCostsResponse.Data.Result.OrganizationCostsResult;


public class QueryUsageCosts {

    public QueryUsageCosts(final OpenAIClient client) {
        this.client = client;
    }

    public List<Map<String,Object>> query(final int lastNdays) {
        final long startTime = LocalDate
                                .now(ZoneOffset.UTC)
                                .minusDays(30)
                                .atStartOfDay(ZoneOffset.UTC)
                                .toEpochSecond();

        final long endTime = Instant.now().getEpochSecond();

        final UsageCostsParams params = UsageCostsParams
                                            .builder()
                                            .startTime(startTime)
                                            .endTime(endTime)
                                            .bucketWidth(UsageCostsParams.BucketWidth._1D)
                                            .addGroupBy(UsageCostsParams.GroupBy.PROJECT_ID)
                                            .addGroupBy(UsageCostsParams.GroupBy.LINE_ITEM)
                                            .limit(30)
                                            .build();

        final UsageCostsResponse response = client
                                                .admin()
                                                .organization()
                                                .usage()
                                                .costs(params);

        return parseResponse(response);
    }

    public String queryFormatted(final int lastNdays) {
        return query(lastNdays)
                .stream()
                .map(it -> formatCostItem(it))
                .collect(Collectors.joining("\n"));
    }


    private List<Map<String,Object>> parseResponse(final UsageCostsResponse response) {
        final List<Map<String,Object>> items = new ArrayList<>();

        response.data().forEach(bucket -> {
            final LocalDateTime bucketStart = toLocalDateTime(bucket.startTime());
            final LocalDateTime bucketEnd = toLocalDateTime(bucket.endTime());

            bucket.results().forEach(result -> {
                if (!result.isOrganizationCosts()) {
                    return;
                }

                final OrganizationCostsResult cost = result.asOrganizationCosts();
                final double value = cost.amount()
                                         .flatMap(amount -> amount.value())
                                         .orElse(0.0);

                final String currency = cost.amount()
                                            .flatMap(amount -> amount.currency())
                                            .orElse("unknown");

                final String projectId = cost.projectId().orElse(null);
                final String lineItem = cost.lineItem().orElse(null);
                final String apiKeyId = cost.apiKeyId().orElse(null);

                final Map<String,Object> item = new HashMap<>();
                item.put("bucketStart", bucketStart);
                item.put("bucketEnd", bucketEnd);
                item.put("value", value);
                item.put("currency", currency);
                item.put("projectId", projectId);
                item.put("lineItem", lineItem);
                item.put("apiKeyId", apiKeyId);

                items.add(item);
            });
        });

        return items;
    }

    private String formatCostItem(final Map<String,Object> item) {
        return String.format(
                "%s – %s | %.6f %s | project=%s | lineItem=%s | apiKey=%s%n",
                item.get("bucketStart"),
                item.get("bucketEnd"),
                item.get("value"),
                item.get("currency"),
                item.getOrDefault("projectId", "-"),
                item.getOrDefault("lineItem", "-"),
                item.getOrDefault("apiKeyId", "-"));
    }

    private LocalDateTime toLocalDateTime(final long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }


    private final OpenAIClient client;
}
