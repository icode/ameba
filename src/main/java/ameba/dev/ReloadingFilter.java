package ameba.dev;

import ameba.Ameba;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author icode
 */
@Provider
@PreMatching
@Priority(0)
public class ReloadingFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ReloadingClassLoader classLoader = (ReloadingClassLoader) Ameba.getApp().getClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        //classLoader.detectChanges();
    }
}
