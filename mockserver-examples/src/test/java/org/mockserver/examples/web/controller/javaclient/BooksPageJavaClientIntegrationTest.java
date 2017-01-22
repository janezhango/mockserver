package org.mockserver.examples.web.controller.javaclient;

import org.junit.runner.RunWith;
import org.mockserver.examples.configuration.RootConfiguration;
import org.mockserver.examples.web.configuration.WebMvcConfiguration;
import org.mockserver.examples.web.controller.BooksPageIntegrationTest;
import org.mockserver.examples.web.controller.PropertyMockingApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author jamesdbloom
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration(
                classes = {
                        RootConfiguration.class
                },
                initializers = PropertyMockingApplicationContextInitializer.class
        ),
        @ContextConfiguration(
                classes = {
                        WebMvcConfiguration.class
                }
        )
})
@ActiveProfiles(profiles = {"javaClient"})
public class BooksPageJavaClientIntegrationTest extends BooksPageIntegrationTest {

}
