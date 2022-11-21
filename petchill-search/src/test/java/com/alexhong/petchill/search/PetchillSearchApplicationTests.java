package com.alexhong.petchill.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.SourceConfigBuilders;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.transform.Source;
import co.elastic.clients.util.ObjectBuilder;
import lombok.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

@SpringBootTest
public class PetchillSearchApplicationTests {

	@Autowired
	ElasticsearchClient elasticsearchClient;


	public static ExecutorService service = Executors.newFixedThreadPool(10);


	@Test
	void searchData() throws IOException {

		SearchRequest.Builder builder = new SearchRequest.Builder();

		Query.Builder builder1 = new Query.Builder();
		Query build = builder1.
				match(m -> m.field("address").query("mill"))
				.build();

		builder.index("newbank")
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
				);
//				.query(q->q
//						.match(m->m.field("address").query("mill"))
//				)
//				.size(10);

        builder.query(build);


		SearchRequest build1 = builder.build();
//		System.out.println(build1.source().toString());
//		System.out.println(build1._DESERIALIZER);


		SearchResponse<bank> newbank = elasticsearchClient.search(build1,
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



	@Test
	void threadPool(){
		/***
		 * corePoolSize
		 * maximumPoolSize
		 * keepAliveTime
		 * unit
		 * workQueue
		 * threadFactory
		 * handler
		 *
		 * working ordering
		 *
		 */
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
				200,
				10,
				TimeUnit.SECONDS,
				new LinkedBlockingDeque<>(100000),
				Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.AbortPolicy());
//		Executors.newCachedThreadPool();
//		Executors.newFixedThreadPool(5);
//		Executors.newScheduledThreadPool()
	}


	public static void main(String[] args) throws ExecutionException, InterruptedException {
		System.out.println("start");
//		CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
//			int i = 10 / 2;
//			System.out.println(i);
//		}, service);
//		CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//			int i = 10 / 0;
//			return i;
//		}, service).whenComplete((res, exception)->{
//			System.out.println("Finished and result:" + res + ";exception:" + exception);
//		}).exceptionally(throwable->{
//			return 10;
//		});
//		CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//			int i = 10 / 0;
//			return i;
//		}, service).handle((res, e)->{
//			if(res!=null){
//				return res*2;
//			}
//			if(e!=null){
//				return 0;
//			}
//			return 0;
//		});

//		CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//			int i = 10 / 0;
//			return i;
//		}, service).thenRunAsync(()->{
//			System.out.println("task2 start");
//		});

//		CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//			int i = 10 / 1;
//			return i;
//		}, service).thenApplyAsync(res -> res);
//		System.out.println(future.get());
//		System.out.println("end");

//		CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
//			int i = 10 / 1;
//			System.out.println("task 1 finish");
//			return i;
//		}, service);
//
//		CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> "hello", service);

//		future01.runAfterBothAsync(future02, () -> {
//			System.out.println("task3 finished");
//		}, service);

//		future01.thenAcceptBothAsync(future02, (f1, f2)->{
//			System.out.println(f1 + f2);
//		}, service);

//		CompletableFuture<String> stringCompletableFuture = future01.thenCombineAsync(future02, (f1, f2) -> f1 + f2 + "hello", service);
//
//		System.out.println(stringCompletableFuture.get());

		CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
			System.out.println("task1");
			return "task1";
		});

		CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
			System.out.println("task2");
			return "task3";
		});

		CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
			System.out.println("task3");
			return "task3";
		});

		CompletableFuture<Void> allof = CompletableFuture.allOf(task1, task2, task3);
		allof.get();
	}


}
