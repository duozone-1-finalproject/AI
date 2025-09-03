import com.example.demo.langgraph.web.state.WebGraph;

@Override
public CompletableFuture<WebGraph> apply(WebGraph state) {
    String company = state.getcorpName();
    String industry = state.getIndustry();

    List<String> queries = List.of(
            company + " " + industry + " 산업 전망",
            company + " " + industry + " 규제",
            company + " " + industry + " 연구개발",
            company + " " + industry + " 시장 동향"
    );

    state.setQueries(queries);
    return CompletableFuture.completedFuture(state);
}
