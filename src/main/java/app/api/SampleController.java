
package app.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

  @PreAuthorize("hasRole('USER')") //NOSONAR
  @GetMapping("/test")
  public String test() {
    return "test";
  }

}
