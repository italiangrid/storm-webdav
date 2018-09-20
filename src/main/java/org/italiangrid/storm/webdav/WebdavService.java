package org.italiangrid.storm.webdav;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class WebdavService {

  public static void main(String[] args) {
    SpringApplication.run(WebdavService.class, args);
  }

}
