package org.italiangrid.storm.webdav.tape;

import java.util.Map;

import javax.validation.Valid;

import org.italiangrid.storm.webdav.tape.model.StageRequest;
import org.italiangrid.storm.webdav.tape.model.StageRequestResponse;
import org.italiangrid.storm.webdav.tape.model.StageStatusRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;

import io.milton.http.exceptions.NotFoundException;


@RestController
public class WlcgMockTapeRestApiController {

  private static Map<String, StageStatusRequest> cache = Maps.newLinkedHashMap();


  @PostMapping({"mock/stage"})
  public StageRequestResponse createRequest(@RequestBody @Valid StageRequest request) {

    StageStatusRequest req = new StageStatusRequest(request.getFiles());
    cache.put(req.getId(), req);
    return new StageRequestResponse(req.getId());
  }

  @GetMapping({"mock/stage/{requestId}"})
  public StageStatusRequest getRequestStatus(
      @PathVariable String requestId) throws NotFoundException {
    if (cache.keySet().contains(requestId)) {
      StageStatusRequest r = cache.get(requestId);
      r.evolve();
      cache.put(r.getId(), r);
      return r;
    } else {
      throw new NotFoundException(requestId + " not found!");
    }
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundException.class)
  public String handleNotFoundException(NotFoundException e) {
    return e.getMessage();
  }
}