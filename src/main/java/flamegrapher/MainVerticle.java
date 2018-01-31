package flamegrapher;

import flamegrapher.backend.JavaFlightRecorder;
import flamegrapher.model.Processes;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {
    
    @Override
    public void start(Future<Void> fut) {
        JavaFlightRecorder jfr = new JavaFlightRecorder(vertx);
        Router router = Router.router(vertx);

        // Bind "/" to our hello message
        router.route("/")
              .handler(routingContext -> {
                  HttpServerResponse response = routingContext.response();
                  response.putHeader("content-type", "text/html")
                          .end("<h1>Hello</h1>");
              });

        router.get("/api/list")
              .handler(rc -> {
                  jfr.list(newFuture(rc));
              });
        
        vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(config().getInteger("http.port", 8080), result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });

    }

    private <T> Future<T> newFuture(RoutingContext rc) {
        Future<T> future = Future.future();
        future.setHandler(result -> {
            if (result.succeeded()) {
                rc.response()
                  .putHeader("content-type", "application/json; charset=utf-8")
                  .end(Json.encodePrettily(result.result()));
            } else {
                managerError(result);
            }
        });
        return future;
    }

    private static void managerError(AsyncResult<?> result) {
        // TODO: Manage errors properly
        System.err.println(result.cause());
    }

    public static void main(String[] args) {
        Vertx.vertx()
             .deployVerticle(MainVerticle.class.getName());
    }
}
