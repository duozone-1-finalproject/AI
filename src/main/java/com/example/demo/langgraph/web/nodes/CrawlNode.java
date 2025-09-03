
@Override
public CompletableFuture<WebGraph> apply(WebGraph state) {
    List<CompletableFuture<Void>> futures = state.getArticles().stream()
            .map(article -> CompletableFuture.runAsync(() -> {
                String content = mcpClient.fetchContent(article.getUrl());
                article.setContent(content);
            }))
            .toList();

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> state);
}


