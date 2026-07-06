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
package com.github.jlangch.venice.util.openai.bugs;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.admin.organization.usage.UsageCostsParams;
import com.openai.models.admin.organization.usage.UsageCostsResponse;
import com.openai.models.admin.organization.usage.UsageCostsResponse.Data.Result.OrganizationCostsResult;


public class UsageCostJsonParserBug {

    private UsageCostJsonParserBug() {}

    public static void main(String[] args) {
        final OpenAIClient client = OpenAIOkHttpClient
                                        .builder()
                                        .adminApiKey(System.getenv("OPENAI_ADMIN_KEY"))
                                        .build();

        final long startTime = LocalDate
                                .now(ZoneOffset.UTC)
                                .minusDays(10)
                                .atStartOfDay(ZoneOffset.UTC)
                                .toEpochSecond();

        final long endTime = Instant.now().getEpochSecond();

        final UsageCostsParams params = UsageCostsParams
                                            .builder()
                                            .startTime(startTime)
                                            .endTime(endTime)
                                            .bucketWidth(UsageCostsParams.BucketWidth._1D)
                                            .addGroupBy(UsageCostsParams.GroupBy.PROJECT_ID)
                                            .addGroupBy(UsageCostsParams.GroupBy.API_KEY_ID)
                                            .addGroupBy(UsageCostsParams.GroupBy.LINE_ITEM)
                                            .limit(180)
                                            .build();

        final UsageCostsResponse response = client
                                                .admin()
                                                .organization()
                                                .usage()
                                                .costs(params);

        response.data().forEach(bucket -> {
            final LocalDateTime bucketStart = toLocalDateTime(bucket.startTime());
            final LocalDateTime bucketEnd = toLocalDateTime(bucket.endTime());

            bucket.results().forEach(result -> {
                if (!result.isOrganizationCosts()) {
                    return;
                }

                final OrganizationCostsResult costs = result.asOrganizationCosts();

                // This custom parser works fine
                final double value = parseAmount_Workaround(costs);

                // This throws an exception
                //final double value = parseAmount_Bug(costs);

                final String currency = costs.amount()
                                            .flatMap(amount -> amount.currency())
                                            .orElse("unknown");

                final String lineItem = costs.lineItem().orElse("-");

                System.out.println(String.format(
                        "%s – %s | %.6f %s | %s",
                        bucketStart,
                        bucketEnd,
                        value,
                        currency,
                        lineItem));

            });
        });
    }

    private static double parseAmount_Workaround(final OrganizationCostsResult costs) {
        // The JSON number parser doesn't work => we roll our own parser!
        return Double.parseDouble(
                    costs.amount().isPresent()
                        ? costs.amount().get()._value().asString().orElse("0.0")
                        : "0.0");
    }

    private static double parseAmount_Bug(final OrganizationCostsResult costs) {
        // This throws an exception
        return costs.amount()
                    .flatMap(amount -> amount.value())  // <== exception
                    .orElse(0.0);
    }


//catch(Exception ex) {
//    // com.openai.errors.OpenAIInvalidDataException: `value` is invalid, received 0E-6176
//    //    at com.openai.core.JsonField.getOptional$openai_java_core(Values.kt:191)
//    //    at com.openai.models.admin.organization.usage.UsageCostsResponse$Data$Result$OrganizationCostsResult$Amount.value(UsageCostsResponse.kt:6680)
//
//    // `value` is invalid, received 0E-6176
//    // `value` is invalid, received 0.003627500000000000000000000000000000
//    // `value` is invalid, received 0.007605000000000000000000000000000000
//    // System.out.println(ex.getMessage());
//    throw ex;
//}

    private static LocalDateTime toLocalDateTime(final long epochSeconds) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(epochSeconds),
                ZoneOffset.UTC);
    }

}
