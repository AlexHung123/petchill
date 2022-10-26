package com.alexhong.petchill.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch.core.CreateResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import lombok.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class PetchillSearchApplicationTests {

	@Autowired
	ElasticsearchClient elasticsearchClient;


	@Test
	void searchData() throws IOException {
		SearchResponse<bank> newbank = elasticsearchClient.search(s -> s
						.index("newbank")
						.aggregations("ageAgg", a->a
								.terms(m->m
										.field("age")
										.size(10)
								)
						).aggregations("avgAgg", a->a
								.avg(m->m
										.field("age")
								)
						).aggregations("balanceAgg", a->a
								.avg(m->m
										.field("balance")
								)
						)
						.query(q->q
								.match(m->m.field("address").query("mill"))
						)
					.size(10),
				bank.class);
		System.out.println(newbank.took());
		System.out.println(newbank.hits().total().value());
		newbank.hits().hits().forEach(e-> System.out.println(e.source().toString()));

		Aggregate aggregate = newbank.aggregations().get("ageAgg");
		LongTermsAggregate lterms = aggregate.lterms();
		Buckets<LongTermsBucket> buckets = lterms.buckets();
		for (LongTermsBucket b : buckets.array()) {
			System.out.println(b.key() + " : " + b.docCount());
		}

		AvgAggregate avgregate = newbank.aggregations().get("avgAgg").avg();
		System.out.println(avgregate.value());

		AvgAggregate avgregateBalance = newbank.aggregations().get("balanceAgg").avg();
		System.out.println(avgregateBalance.value());

	}

	@Test
	void updateData() throws IOException {
		Map<String, Object> map = new HashMap<>();
		map.put("age", 36);

		UpdateResponse<person> response = elasticsearchClient.update(e -> e.index("user").id("1").doc(map), person.class);

		System.out.println(response.result());
	}

	@Test
	void searchData1() throws IOException {
		GetResponse<person> response = elasticsearchClient.get(e -> e.index("user").id("1"), person.class);
		System.out.println(response.source().toString());
	}


}
