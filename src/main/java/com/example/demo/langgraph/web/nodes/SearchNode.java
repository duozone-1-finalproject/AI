import java.util.Stack;
import java.util.concurrent.CompletableFuture;

@Override
public CompletableFuture<NewsState> apply(NewsState state) {
    Stack<E> mcpClient;
    List<CompletableFuture<List<Article>>> futures = state.getQueries().stream()
            .map(q -> CompletableFuture.supplyAsync(() -> mcpClient.search(q, 5)))
            .toList();

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<Article> allArticles = new ArrayList<>();
                futures.forEach(f -> {
                    try { allArticles.addAll(f.get()); } catch (Exception e) { e.printStackTrace(); }
                });
                state.setArticles(allArticles);
                return state;
            });
}

