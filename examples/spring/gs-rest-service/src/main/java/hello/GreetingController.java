package hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@Slf4j
public class GreetingController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        long id = counter.incrementAndGet();
        log.info("Request /greeting: '{}'", id);
        Greeting greeting = new Greeting();
        greeting.setId(id);
        greeting.setContent(String.format(template, name));
        return greeting;
    }

    @RequestMapping("/reverse")
    public Greeting reverse(@RequestParam(value = "caps", required = false) Capitalisation caps,
                             @RequestBody Greeting greetIn) {
      StringBuilder sb = new StringBuilder(greetIn.getContent());
      String reverse = sb.reverse().toString();
      Greeting greetOut = new Greeting();
      if (caps == null) {
        greetOut.setContent(reverse);
      } else {
        switch (caps) {
          case UPPER:
            greetOut.setContent(reverse.toUpperCase());
            break;
          case LOWER:
            greetOut.setContent(reverse.toLowerCase());
            break;
          default:
            greetOut.setContent(reverse);
        }
      }
      return greetOut;
    }

    @PostMapping(value = "/count")
    public void count(@RequestBody Object request, @RequestHeader HttpHeaders headers) {
        log.info("Request /count: '{}', {}", request, headers);
    }

    public static enum Capitalisation {
      UPPER,
      LOWER
    }
}
