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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    public BigDecimal aggregateCosts(final List<Map<String,Object>> costItems) {
        return costItems.isEmpty()
                ? new BigDecimal("0.00")
                : costItems
                    .stream()
                    .map(it -> (String)it.getOrDefault("value", "0.0"))
                    .map(it -> new BigDecimal(it))
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b))
                    .setScale(2, RoundingMode.HALF_UP);
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

                try {
                    final OrganizationCostsResult cost = result.asOrganizationCosts();

                    // The JSON number parser doesn't work => we roll our own parser!
                    final double value = Double.parseDouble(
                                            cost.amount().isPresent()
                                                ? cost.amount().get()._value().asString().orElse("0.0")
                                                : "0.0");


// This throws an exception
//                    final double value = cost.amount()
//                                             .flatMap(amount -> amount.value())
//                                             .orElse(0.0);

                    final String currency = cost.amount()
                                                .flatMap(amount -> amount.currency())
                                                .orElse("unknown");

                    final String projectId = cost.projectId().orElse(null);
                    final String lineItem = cost.lineItem().orElse(null);
                    final String apiKeyId = cost.apiKeyId().orElse(null);

                    final Map<String,Object> item = new LinkedHashMap<>();
                    item.put("bucket-start", bucketStart);
                    item.put("bucket-end", bucketEnd);
                    item.put("value", value);
                    item.put("currency", currency);
                    item.put("project-id", projectId);
                    item.put("lineitem", lineItem);
                    item.put("api-key-id", apiKeyId);

                    items.add(item);
                }
                catch(Exception ex) {
                    // com.openai.errors.OpenAIInvalidDataException: `value` is invalid, received 0E-6176
                    //    at com.openai.core.JsonField.getOptional$openai_java_core(Values.kt:191)
                    //    at com.openai.models.admin.organization.usage.UsageCostsResponse$Data$Result$OrganizationCostsResult$Amount.value(UsageCostsResponse.kt:6680)

                    // `value` is invalid, received 0E-6176
                    // `value` is invalid, received 0.003627500000000000000000000000000000
                    // `value` is invalid, received 0.007605000000000000000000000000000000
                    // System.out.println(ex.getMessage());
                    throw ex;
                }
            });
        });

        return items;
    }

    private String formatCostItem(final Map<String,Object> item) {
        return String.format(
                "%s – %s | %.6f %s | project=%s | lineItem=%s | apiKey=%s%n",
                item.get("bucket-start"),
                item.get("bucket-end"),
                item.get("value"),
                item.get("currency"),
                item.getOrDefault("project-id", "-"),
                item.getOrDefault("lineitem", "-"),
                item.getOrDefault("api-key-id", "-"));
    }

    private LocalDateTime toLocalDateTime(final long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }


    private final OpenAIClient client;
}
