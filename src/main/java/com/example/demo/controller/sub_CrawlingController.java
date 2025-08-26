// 크롤링된 뉴스 fast api 통해서 연동해야함

@RestController
@RequestMapping("/crawl")
public class SubCrawlingController {

    @Value("${fastapi.url:http://localhost:8000}")
    private String fastApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // 여러 기사 JSON 배열 받기
    @GetMapping("/news")
    public ResponseEntity<List<NewsResponseDto>> getNews(@RequestParam String keyword) {
        String url = fastApiUrl + "/news?keyword=" + keyword;

        // JSON 배열 → NewsResponseDto 리스트로 매핑
        ResponseEntity<NewsResponseDto[]> response = restTemplate.getForEntity(url, NewsResponseDto[].class);

        // 배열 → 리스트 변환
        List<NewsResponseDto> articles = Arrays.asList(response.getBody());

        return ResponseEntity.ok(articles);
    }
}



