package org.italiangrid.storm.webdav.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class SecurityErrorController {

  @ResponseStatus(BAD_REQUEST)
  @RequestMapping("/errors/400")
  String error(RequestRejectedException e) {
    return "errors/400";
  }



}
