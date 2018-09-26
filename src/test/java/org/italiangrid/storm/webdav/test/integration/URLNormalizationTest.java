package org.italiangrid.storm.webdav.test.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

//@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = {WebdavService.class})
//@SpringBootTest
//@AutoConfigureMockMvc
public class URLNormalizationTest {

  static {
    System.setProperty("STORM_WEBDAV_SA_CONFIG_DIR", "src/test/resources/conf/sa.d");
    System.setProperty("STORM_WEBDAV_AUTHORIZATION_DISABLE", "true");
  }

  @Autowired
  MockMvc mvc;

  
  public void testDoubleSlashRequest() throws Exception {
    mvc.perform(get("//test")).andExpect(status().isBadRequest());
  }

}
