---
layout: default
title: Redpipe
github_url: https://github.com/FroMage/redpipe
github_source_url: https://github.com/FroMage/redpipe/tree/master
version: 0.0.2
---

![Logo]({{ "images/redpipe-600.png" }})

Redpipe is a Web Framework that unites the power and versatility of [Eclipse Vert.x](http://vertx.io), the conciseness of 
[JAX-RS](https://javaee.github.io/tutorial/jaxrs002.html#GILIK) (with [Resteasy](http://resteasy.jboss.org)), 
and the non-blocking reactive composition of [RxJava](https://github.com/ReactiveX/RxJava).

The main idea is that with Redpipe you write your endpoints in JAX-RS, using RxJava composition if you want, and underneath
it all, Vert.x is running the show and you can always access it if you need it.

Redpipe is opinionated, favors convention over configuration, and lets you combine the following technologies easily to
write reactive web applications:

- [Eclipse Vert.x](http://vertx.io) and [Netty](https://netty.io), for all the low-level plumbing,
- [JAX-RS](https://javaee.github.io/tutorial/jaxrs002.html#GILIK) ([Resteasy](http://resteasy.jboss.org)), for all your web endpoints,
- [RxJava](https://github.com/ReactiveX/RxJava) 1 and 2 for your reactive code,
- [Vert.x Web](http://vertx.io/docs/vertx-web/java/) for all existing filters, templating support,
- [Swagger](https://swagger.io) to describe your web endpoints,
- [CDI](http://cdi-spec.org) ([Weld](http://weld.cdi-spec.org)) for JAX-RS discovery and injection (optional),
- [Bean Validation](http://beanvalidation.org) ([Hibernate Validator](http://hibernate.org/validator/)) to validate your web endpoints' input (optional),
- Coroutines ([Quasar](http://docs.paralleluniverse.co/quasar/)) to write synchronous-looking reactive code (optional).

## Reactive / Asynchronous super quick introduction

Redpipe is a [reactive asynchronous](http://reactivex.io/intro.html) web framework. 
This mostly means that you should [not make blocking](http://vertx.io/docs/vertx-core/java/#_don_t_call_us_we_ll_call_you)
calls when Redpipe calls your methods, because that would 
[block the Vert.x event loop](http://vertx.io/docs/vertx-core/java/#golden_rule) and would prevent
you from the performance benefits of asynchronous reactive programming.

Typically, instead of making a blocking call, asynchronous APIs will let you register a callback to invoke when
the action is done. But callbacks are [error-prone](http://callbackhell.com/) and do not compose well. 
The [Promise model](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Using_promises) was introduced to
abstract over callbacks and asynchronous computations, where a Promise represents a producer of a value that
you can listen for, and be notified when the value is produced (we call that _resolving_ a Promise).

RxJava is an implementation of the Promise model with support for single-value Promises 
([`Single`](http://reactivex.io/documentation/single.html)) and
multiple-value Promise streams ([`Observable`](http://reactivex.io/documentation/observable.html)).

The idea of Redpipe is that you can either return normal values from your methods (if your code does not
need to block), or RxJava promises (if it does), which Redpipe will register on, and be notified when the
promises resolve and forward the resolved values to the client.

## Source code / license / issue tracker

Redpipe is [open-source software](https://github.com/FroMage/redpipe/) licensed under the 
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

You can file issues at [our GitHub issues tracker](https://github.com/FroMage/redpipe/issues),
or better yet: send us pull requests. 

## Set-up

Include the following dependency in your `pom.xml`:

{% highlight xml %}
<dependency>
  <groupId>net.redpipe</groupId>
  <artifactId>redpipe-engine</artifactId>
  <version>{{page.version}}</version>
</dependency>
{% endhighlight %}

## Quick Examples

Create a `Main` class to start your server:

{% highlight java %}
import net.redpipe.engine.core.Server;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Main {
  public static void main( String[] args ){
    new Server()
     .start(HelloResource.class)
     .subscribe(
        v -> System.err.println("Server started"),
        x -> x.printStackTrace());
  }
}
{% endhighlight %}

### Creating a resource

Create a resource:

{% highlight java %}
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class HelloResource {
  @GET
  public String hello() {
    return "Hello World";
  }
}
{% endhighlight %}

Start your `Main` program and if you head over to [http://localhost:9000](http://localhost:9000) you should see `Hello World`.

### Creating a reactive resource

If you want to return a reactive resource, you can simply return a reactive type from your resource,
such as those provided by RxJava.

For a reactive Hello World, simply add this method to your resource:

{% highlight java %}
@Path("reactive")
@GET
public Single<String> helloReactive() {
  return Single.just("Hello Reactive World");
}
{% endhighlight %}

Restart your `Main` program and if you head over to [http://localhost:9000/reactive](http://localhost:9000/reactive)
you should see `Hello Reactive World`.

### Streams

If you want to return a stream, you have three options for how to serialise them (see below).

To send Hello World as a stream, simply add this method to your resource:

{% highlight java %}
@Stream
@Path("stream")
@GET
public Observable<String> helloStream() {
  return Observable.from(new String[] {"Hello", "World"});
}
{% endhighlight %}

Restart your `Main` program and if you head over to [http://localhost:9000/stream](http://localhost:9000/stream)
you should see `HelloWorld`.

## RxJava support

Out of the box, we support RxJava's [`Single`](http://reactivex.io/documentation/single.html)
and [`Observable`](http://reactivex.io/documentation/observable.html), [`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html) and [`Maybe`](http://reactivex.io/RxJava/javadoc/io/reactivex/Maybe.html) types for both RxJava 1 and 2,
although use of RxJava 2 is recommended because RxJava 1 is deprecated.

If your resource returns a `Single<T>`, the `T` will be sent to your client asynchronously as if you
returned it directly. In particular, standard and custom
[`MessageBodyWriter<T>`](https://javaee.github.io/javaee-spec/javadocs/javax/ws/rs/ext/MessageBodyWriter.html)
apply as soon as the `Single` is _resolved_, as do 
[interceptors](http://docs.jboss.org/resteasy/docs/3.1.4.Final/userguide/html/Interceptors.html).

If your resource returns an `Observable<T>`:

- By default, every item will be collected, and once complete, assembled in a `List<T>` and treated
  as if your method had returned it directly (standard and custom body writers and interceptors apply
  to it).
- If you annotate your method with `@Stream` (from `org.jboss.resteasy.annotations`), then every item
  will be sent to the client as soon as it is produced (again, via standard and custom body writers).
  This is mostly useful for streaming bytes, buffers, or strings, which can be split up and buffered.
- If you annotate your method with `@Produces(MediaType.SERVER_SENT_EVENTS)`, then every item will
  be sent to the client over 
  [Server-Sent Events](https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events)
  (SSE). As always, standard and custom body writers
  are called to serialise your entities to events.

If your resource returns a `Completable`, an HTTP Response with status code 204 will be returned once the completable 
is _resolved_.

If your resource returns a `Maybe<T>`, then the following response will be sent asynchronously once the maybe is
resolved:
- If the maybe is empty: an empty HTTP response using __HTTP status code `404`__ indicating the resource has not been found
- If the maybe has been fulfilled with an object of type `T`: an HTTP response __with status code `200`__, 
using the proper [`MessageBodyWriter<T>`](https://javaee.github.io/javaee-spec/javadocs/javax/ws/rs/ext/MessageBodyWriter.html) 

## Fibers

The optional module `redpipe-fibers` allows you to write your reactive code in _fibers_, which are
light-weight threads, also known as _coroutines_, while interacting seamlessly with RxJava.

Consider the following traditional RxJava code to forward the result of two web service invocations:

{% highlight java %}
@Path("composed")
@GET
public Single<String> helloComposed(@Context Vertx vertx) {
  Single<String> request1 = get(vertx, getURI(HelloResource::hello));
  Single<String> request2 = get(vertx, getURI(HelloResource::helloReactive));
      
  return request1.zipWith(request2, (hello1, hello2) -> hello1 + "\n" + hello2);
}

private Single<String> get(Vertx vertx, URI uri){
  WebClient client = WebClient.create(vertx);
  Single<HttpResponse<Buffer>> responseHandler = 
    client.get(uri.getPort(), uri.getHost(), 
               uri.getPath()).rxSend();

  return responseHandler.map(response -> response.body().toString());
}
{% endhighlight %}

Rather than composing sequential `Single` values, you can write a _fiber_ that can
_await_ those values in what now looks like sequential code:

{% highlight java %}
@Path("fiber")
@GET
public Single<String> helloFiber(@Context Vertx vertx) {
  return Fibers.fiber(() -> {
    String hello1 = Fibers.await(get(vertx, getURI(HelloResource::hello)));
    String hello2 = Fibers.await(get(vertx, getURI(HelloResource::helloReactive)));
    
    return hello1 + "\n" + hello2;
  });
}
{% endhighlight %}

A _fiber_, here, is also a `Single` which can be integrated with the rest of your RxJava
operations, and it can `await` RxJava `Single` values to obtain their resolved value.

You can call `await` from any fiber body, or from any method annotated with `@Suspendable`.

You need to import the following module in order to use fibers:

{% highlight xml %}
<dependency>
  <groupId>net.redpipe</groupId>
  <artifactId>redpipe-fibers</artifactId>
  <version>{{page.version}}</version>
</dependency>
{% endhighlight %}

And also the following Maven build plug-in to set-up your fibers at compile-time:

{% highlight xml %}
<build>
  <plugins>
    <plugin>
      <groupId>com.vlkan</groupId>
      <artifactId>quasar-maven-plugin</artifactId>
      <version>0.7.9</version>
      <configuration>
        <check>true</check>
        <debug>true</debug>
        <verbose>true</verbose>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>instrument</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
{% endhighlight %}

See [Quasar's documentation](http://docs.paralleluniverse.co/quasar/) for more information on fibers. 

## Reverse-routing

In order to get URIs that map to resource methods, you can use the redpipe routing module:

{% highlight xml %}
<dependency>
  <groupId>net.redpipe</groupId>
  <artifactId>redpipe-router</artifactId>
  <version>{{page.version}}</version>
</dependency>
{% endhighlight %}

With this module, you can simply get a `URI` for a resource method `HelloResource.hello` by calling
the `Router.getURI()` method and passing it a reference to your resource method and any required
parameters: 

{% highlight java %}
URI uri1 = Router.getURI(HelloResource::hello);
URI uri2 = Router.getURI(HelloResource::helloWithParameters, "param1", 42);
{% endhighlight %}

Within templates you can use the `context.route` method which takes a string literal pointing to your
resource method, and any required parameters:

{% highlight html %}
<a href="${context.route('WikiResource.renderPage', page)}">${page}</a>
{% endhighlight %}

## Resource scanning

You can either declare your JAX-RS resources and providers when instantiating the `Server` (as shown
above), or you can use two options to scan your packages to discover them.

### Fast-classpath-scanner

If you include this dependency, [fast-classpath-scanner](https://github.com/lukehutch/fast-classpath-scanner)
will be used to scan your classpath for resources and providers:

{% highlight xml %}
<dependency>
  <groupId>net.redpipe</groupId>
  <artifactId>redpipe-fast-classpath-scanner</artifactId>
  <version>{{page.version}}</version>
</dependency>
{% endhighlight %}

You just need to set the [`scan` configuration](#configuration) to an array of package names to scan.

### CDI

Alternately, you can delegate scanning of resources and providers to [CDI](http://cdi-spec.org):

{% highlight xml %}
<dependency>
  <groupId>net.redpipe</groupId>
  <artifactId>redpipe-cdi</artifactId>
  <version>{{page.version}}</version>
</dependency>
{% endhighlight %}

All your jars containing a `META-INF/beans.xml` file will be scanned by CDI and your resources and providers
will become CDI beans, with CDI injection supported.

Note that this uses [Weld](http://weld.cdi-spec.org) and the weld-vertx extension which brings a 
[lot of goodies for CDI and Vert.x](http://docs.jboss.org/weld/weld-vertx/latest/).

## Configuration

Unless you call `Server.start()` with a `JsonObject` configuration to override it, 
the `conf/config.json` file will be loaded and used for configuration. 

In addition to the standard [Vertx options](https://vertx.io/docs/apidocs/io/vertx/core/VertxOptions.html), 
these are the configuration options available:

<table>
 <caption>General options</caption>
 <tr>
  <th>Name</th>
  <th>Type</th>
  <th>Description</th>
  <th>Default</th>
 </tr>
 <tr>
  <td>db_url</td>
  <td>String</td>
  <td>Jdbc Url to use for connections to the database.</td>
  <td>jdbc:hsqldb:file:db/wiki</td>
 </tr>
 <tr>
  <td>db_driver</td>
  <td>String</td>
  <td>Jdbc driver class.</td>
  <td>org.hsqldb.jdbcDriver</td>
 </tr>
 <tr>
  <td>db_max_pool_size</td>
  <td>Integer</td>
  <td>Maximum pool size for database connections.</td>
  <td>30</td>
 </tr>
 <tr>
  <td>http_port</td>
  <td>Integer</td>
  <td>Http port to bind to.</td>
  <td>9000</td>
 </tr>
 <tr>
  <td>http_host</td>
  <td>String</td>
  <td>Http host to bind to.</td>
  <td>0.0.0.0 (all interfaces)</td>
 </tr>
</table> 

<table>
 <caption>For the `redpipe-fast-classpath-scanner` module</caption>
 <tr>
  <th>Name</th>
  <th>Type</th>
  <th>Description</th>
  <th>Default</th>
 </tr>
 <tr>
  <td>scan</td>
  <td>String[]</td>
  <td>List of packages to scan for JAX-RS resources and providers.</td>
  <td></td>
 </tr>
</table> 

You can access the current configuration in your application via the `AppGlobals.getConfig()` method.

## Injection

On top of optional CDI support, JAX-RS supports injection of certain resources via the `@Context` annotation
on method parameters and members. Besides the regular JAX-RS resources, the following resources can be
injected:

<table>
 <caption>Injectable global resources</caption>
 <tr>
  <th>Type</th>
  <th>Description</th>
 </tr>
 <tr>
  <td>io.vertx.core.Vertx</td>
  <td>The Vert.x instance.</td>
 </tr>
 <tr>
  <td>io.vertx.reactivex.core.Vertx</td>
  <td>The Vert.x instance (RxJava 2).</td>
 </tr>
 <tr>
  <td>io.vertx.rxjava.core.Vertx</td>
  <td>The Vert.x instance (RxJava 1).</td>
 </tr>
 <tr>
  <td>net.redpipe.engine.core.AppGlobals</td>
  <td>A global context object.</td>
 </tr>
</table> 

<table>
 <caption>Injectable per-request resources</caption>
 <tr>
  <th>Type</th>
  <th>Description</th>
 </tr>
 <tr>
  <td>io.vertx.reactivex.ext.web.RoutingContext</td>
  <td>The Vert.x Web <code>RoutingContext</code> (RxJava 2).</td>
 </tr>
 <tr>
  <td>io.vertx.rxjava.ext.web.RoutingContext</td>
  <td>The Vert.x Web <code>RoutingContext</code> (RxJava 1).</td>
 </tr>
 <tr>
  <td>io.vertx.reactivex.core.http.HttpServerRequest</td>
  <td>The Vert.x request (RxJava 2).</td>
 </tr>
 <tr>
  <td>io.vertx.rxjava.core.http.HttpServerRequest</td>
  <td>The Vert.x request (RxJava 1).</td>
 </tr>
 <tr>
  <td>io.vertx.core.http.HttpServerRequest</td>
  <td>The Vert.x request.</td>
 </tr>
 <tr>
  <td>io.vertx.reactivex.core.http.HttpServerResponse</td>
  <td>The Vert.x response (RxJava 2).</td>
 </tr>
 <tr>
  <td>io.vertx.rxjava.core.http.HttpServerResponse</td>
  <td>The Vert.x response (RxJava 1).</td>
 </tr>
 <tr>
  <td>io.vertx.core.http.HttpServerResponse</td>
  <td>The Vert.x response.</td>
 </tr>
 <tr>
  <td>io.vertx.reactivex.ext.auth.AuthProvider</td>
  <td>The Vert.x <code>AuthProvider</code> instance, if any (defaults to <code>null</code>) (RxJava 2).</td>
 </tr>
 <tr>
  <td>io.vertx.rxjava.ext.auth.AuthProvider</td>
  <td>The Vert.x <code>AuthProvider</code> instance, if any (defaults to <code>null</code>) (RxJava 1).</td>
 </tr>
 <tr>
  <td>io.vertx.reactivex.ext.auth.User</td>
  <td>The Vert.x <code>User</code>, if any (defaults to <code>null</code>) (RxJava 2).</td>
 </tr>
 <tr>
  <td>io.vertx.rxjava.ext.auth.User</td>
  <td>The Vert.x <code>User</code>, if any (defaults to <code>null</code>) (RxJava 1).</td>
 </tr>
 <tr>
  <td>io.vertx.reactivex.ext.web.Session</td>
  <td>The Vert.x Web <code>Session</code> instance, if any (defaults to <code>null</code>) (RxJava 2).</td>
 </tr>
 <tr>
  <td>io.vertx.rxjava.ext.web.Session</td>
  <td>The Vert.x Web <code>Session</code> instance, if any (defaults to <code>null</code>) (RxJava 1).</td>
 </tr>
</table> 

## Templating

We support the following plugable template engines, which you just have to add a dependency on:

<table>
 <caption>Plugable template engine modules</caption>
 <tr>
  <th>Name</th>
  <th>Dependency</th>
 </tr>
 <tr>
  <td><a href="http://freemarker.apache.org">FreeMarker</a></td>
  <td>
{% highlight xml %}
<dependency>
  <groupId>net.redpipe</groupId>
  <artifactId>redpipe-templating-freemarker</artifactId>
  <version>{{page.version}}</version>
</dependency>
{% endhighlight %}
  </td>
 </tr>
</table>

In order to declare templates, simply place them in the `src/main/resources/templates` folder. For
example, here's our `src/main/resources/templates/index.ftl` template:

{% highlight html %}
<html>
 <head>
  <title>${context.title}</title>
 </head>
 <body>${context.message}</body>
</html>
{% endhighlight %}

In order to return a rendered template, just return them from your resource, directly or as a `Single`:

{% highlight java %}
@GET
@Path("template")
public Template template(){
  return new Template("templates/index.ftl")
          .set("title", "My page")
          .set("message", "Hello");
}
{% endhighlight %}

### Writing your own template renderer

You can write your own template renderer by declaring a `META-INF/services/net.redpipe.engine.template.TemplateRenderer` file in your
`src/main/resources` folder, containing the name of the class that extends the 
`net.redpipe.engine.template.TemplateRenderer` interface.

For example, this is the FreeMarker renderer, which simply wraps the [Vert.x Web support for 
freemarker](https://github.com/vert-x3/vertx-web/tree/master/vertx-template-engines/vertx-web-templ-freemarker):

{% highlight java %}
public class FreeMarkerTemplateRenderer implements TemplateRenderer {

  private final FreeMarkerTemplateEngine templateEngine = 
    FreeMarkerTemplateEngine.create();

  @Override
  public boolean supportsTemplate(String name) {
    return name.toLowerCase().endsWith(".ftl");
  }

  @Override
  public Single<Response> render(String name, 
      Map<String, Object> variables) {
    RoutingContext context = ResteasyProviderFactory.getContextData(RoutingContext.class);
    for (Entry<String, Object> entry : variables.entrySet()) {
      context.put(entry.getKey(), entry.getValue());
    }
    return templateEngine.rxRender(context, name)
            .map(buffer -> Response.ok(buffer, MediaType.TEXT_HTML).build());
  }
}
{% endhighlight %}


## Serving static files

If you want to serve static files you can place them in `src/main/resources/webroot` and declare the
following resource:

{% highlight java %}
@Path("")
public class AppResource extends FileResource {

  @Path("webroot{path:(/.*)?}")
  @GET
  public Response get(@PathParam("path") String path) throws IOException {
    return super.getFile(path);
  }
}
{% endhighlight %}

Your files will then be accessible from the `/webroot/` path prefix.

### Serving `index.html` at `/` by default

Usually web servers do this by default.

{% highlight java %}
@Path("/{path:(.*)?}")
public class AppResource extends FileResource {

  @GET
  public Response index(@PathParam("path") String path) throws IOException {
    return super.getFile(path.equals("") ? "index.html" : path);
  }
}
{% endhighlight %}

Your home page will then be accessible from the root path (eg: [http://localhost:9000](http://localhost:9000)).


## Plugins

You can write plugins by declaring a `META-INF/services/net.redpipe.engine.spi.Plugin` file in your
`src/main/resources` folder, containing the name of the class that extends the 
`net.redpipe.engine.spi.Plugin` class.

From there you can hook into various parts of the application (start-up, requests…).

## Swagger

By default, your application includes [Swagger](https://swagger.io) OpenAPI Specification (OAS) generation, 
located at [/swagger.json](http://localhost:9000/swagger.json)
and [/swagger.yaml](http://localhost:9000/swagger.json).

## Examples

Check out the following examples for best-practices and real-world usage:

- [Hello World]({{page.github_source_url}}/redpipe-example-helloworld) (Reactive, Fibers, Templates)
- Wiki (Reactive, DB, Fibers, Auth, Templates, REST, JWT, SocksJS, Angular) 
 - [With Keycloak and Jooq]({{page.github_source_url}}/redpipe-example-wiki-keycloak-jooq)
 - [With Apache Shiro and Jdbc]({{page.github_source_url}}/redpipe-example-wiki-shiro-jdbc)
- [Kafka]({{page.github_source_url}}/redpipe-example-kafka) (SSE)

## How-to

### DB

You can get a `Single<SQLConnection>` with `SQLUtil.getConnection()`. Alternately, you can call
`SQLUtil.doInConnection` as such:

{% highlight java %}
@POST
@Path("pages")
public Single<Response> apiCreatePage(JsonObject page, 
                                      @Context HttpServerRequest req){
  JsonArray params = new JsonArray();
  params.add(page.getString("name"))
        .add(page.getString("markdown"));

  return SQLUtil.doInConnection(connection -> connection.rxUpdateWithParams(SQL.SQL_CREATE_PAGE, params))
          .map(res -> Response.status(Status.CREATED).build());
}
{% endhighlight %}

#### Custom data-base set-up

You can either set-up your database using the config options `db_url`, `db_driver`, and `db_max_pool_size`,
or you can override the `createDb` method in `Server`, as in the 
[Jooq]({{page.github_source_url}}/redpipe-example-wiki-keycloak-jooq/src/main/java/net/redpipe/example/wiki/keycloakJooq/WikiServer.java#L89)
example:

{% highlight java %}
@Override
protected SQLClient createDbClient(JsonObject config) {
  JsonObject myConfig = new JsonObject();
  if(config.containsKey("db_host"))
      myConfig.put("host", config.getString("db_host"));
  if(config.containsKey("db_port"))
      myConfig.put("port", config.getInteger("db_port"));
  if(config.containsKey("db_user"))
      myConfig.put("username", config.getString("db_user"));
  if(config.containsKey("db_pass"))
      myConfig.put("password", config.getString("db_pass"));
  if(config.containsKey("db_name"))
      myConfig.put("database", config.getString("db_name"));
  myConfig.put("max_pool_size", config.getInteger("db_max_pool_size", 30));
  
  Vertx vertx = AppGlobals.get().getVertx();
  AsyncSQLClient dbClient =  PostgreSQLClient.createNonShared(vertx, myConfig);
  AsyncJooqSQLClient client = AsyncJooqSQLClient.create(vertx, dbClient);

  Configuration configuration = new DefaultConfiguration();
  configuration.set(SQLDialect.POSTGRES);

  PagesDao dao = new PagesDao(configuration);
  dao.setClient(client);
  
  AppGlobals.get().setGlobal("dao", dao);
  
  return dbClient;
}
{% endhighlight %}

### Authentication

By default, no authentication is set up, so if you want to set up authentication you can override
the `Server.setupAuthenticationRoutes` method, as in the following two examples.

If you do set up authentication, your `AuthProvider` will be injectable in your resources, as will the
`User` and `Session`.

#### Authorization

You can use the following annotations on your resource methods/classes to enable resource-level authorization 
checks:

- `@RequiresPermissions({ "perm1", "perm2"})`: requires that the current user exists and has both permissions,
- `@RequiresUser`: requires that the current user exists,
- `@NoAuthFilter`: disables authorization checks,
- `@NoAuthRedirect`: return an HTTP FORBIDDEN (403) instead of a redirect to the login page, if authorization fails.
 
#### Keycloak

Install [Keycloak](http://www.keycloak.org), start and configure it.

Add the following dependency:

{% highlight xml %}
<dependency>
  <groupId>io.vertx</groupId>
  <artifactId>vertx-auth-oauth2</artifactId>
</dependency>
{% endhighlight %}

For the 
[Keycloak example]({{page.github_source_url}}/redpipe-example-wiki-keycloak-jooq/src/main/java/net/redpipe/example/wiki/keycloakJooq/WikiServer.java#L37),
we need this set-up:

{% highlight java %}
@Override
protected AuthProvider setupAuthenticationRoutes() {
  JsonObject keycloackConfig = AppGlobals.get().getConfig().getJsonObject("keycloack");
  OAuth2Auth authWeb = KeycloakAuth.create(AppGlobals.get().getVertx(), keycloackConfig);
  OAuth2Auth authApi = KeycloakAuth.create(AppGlobals.get().getVertx(), 
                                           OAuth2FlowType.PASSWORD, 
                                           keycloackConfig);
  
  OAuth2AuthHandler authHandler = 
    OAuth2AuthHandler.create((OAuth2Auth) authWeb, 
                             "http://localhost:9000/callback");
  Router router = AppGlobals.get().getRouter();
  AuthProvider authProvider = AuthProvider.newInstance(authWeb.getDelegate());
  router.route().handler(UserSessionHandler.create(authProvider));

  authHandler.setupCallback(router.get("/callback"));
  
  router.route().handler(authHandler);
  
  return AuthProvider.newInstance(authApi.getDelegate());
}
{% endhighlight %}

#### Apache Shiro

Add the following dependency:

{% highlight xml %}
<dependency>
  <groupId>io.vertx</groupId>
  <artifactId>vertx-auth-shiro</artifactId>
</dependency>
{% endhighlight %}

For the 
[Apache Shiro example]({{page.github_source_url}}/redpipe-example-wiki-shiro-jdbc/src/main/java/redpipe/redpipe/example/wiki/shiroJdbc/WikiServer.java#L29),
we need this set-up:

{% highlight java %}
@Override
protected AuthProvider setupAuthenticationRoutes() {
  AppGlobals globals = AppGlobals.get();
  AuthProvider auth = ShiroAuth.create(globals.getVertx(), 
                                       new ShiroAuthOptions()
                                         .setType(ShiroAuthRealmType.PROPERTIES)
                                         .setConfig(new JsonObject()
                                           .put("properties_path", 
                                                globals.getConfig().getString("security_definitions"))));
  
  globals.getRouter().route().handler(UserSessionHandler.create(auth));
  
  return null;
}
{% endhighlight %}

Now you can write your handler for the log-in form:

{% highlight java %}
@Path("/")
public class SecurityResource extends BaseSecurityResource {

  @Override
  public Template login(@Context UriInfo uriInfo){
    return new Template("templates/login.ftl")
            .set("title", "Login")
            .set("uriInfo", uriInfo)
            .set("SecurityResource", 
                 BaseSecurityResource.class);
  }
}
{% endhighlight %}

Note that the log-in and log-out handlers are set-up in the `BaseSecurityResource` class,
but you can override them if you need to. 

#### JWT

Add the following dependency:

{% highlight xml %}
<dependency>
  <groupId>io.vertx</groupId>
  <artifactId>vertx-auth-shiro</artifactId>
</dependency>
{% endhighlight %}

For the 
[JWT example]({{page.github_source_url}}/redpipe-example-wiki-shiro-jdbc/src/main/java/net/redpipe/example/wiki/shiroJdbc/WikiServer.java#L29),
we need this set-up:

{% highlight java %}
@Override
protected AuthProvider setupAuthenticationRoutes() {
  AppGlobals globals = AppGlobals.get();
  
  // Your regular authentication
  AuthProvider authProvider = ...;

  // attempt to load a Key file
  JWTAuth jwtAuth = JWTAuth.create(globals.getVertx(), new JWTAuthOptions(keyStoreOptions));
  JWTAuthHandler jwtAuthHandler = JWTAuthHandler.create(jwtAuth);

  globals.setGlobal(JWTAuth.class, jwtAuth);
  globals.getRouter().route().handler(context -> {
    // only filter if we have a header, otherwise 
    // it will try to force auth, regardless whether
    // we want auth
    if(context.request().getHeader(HttpHeaders.AUTHORIZATION) != null)
      jwtAuthHandler.handle(context);
    else
      context.next();
  });
  
  return authProvider;
}
{% endhighlight %}

Then you can set create a resource that will serve your token (in a resource with no
authorization checks):

{% highlight java %}
@Produces("text/plain")
@GET
@Path("token")
public Single<Response> token(@HeaderParam("login") String username, 
                              @HeaderParam("password") String password,
                              @Context JWTAuth jwt,
                              @Context AuthProvider auth){
  
  JsonObject creds = new JsonObject()
          .put("username", username)
          .put("password", password);
  return fiber(() -> {
    User user;
    try {
      user = await(auth.rxAuthenticate(creds));
    }catch(VertxException x) {
      return Response.status(Status.FORBIDDEN).build();
    }
    
    boolean canCreate = await(user.rxIsAuthorised("create"));
    boolean canUpdate = await(user.rxIsAuthorised("update"));
    boolean canDelete = await(user.rxIsAuthorised("delete"));
    JsonArray permissions = new JsonArray();
    if(canCreate)
      permissions.add("create");
    if(canUpdate)
      permissions.add("update");
    if(canDelete)
      permissions.add("delete");
    
    String jwtToken = jwt.generateToken(new JsonObject()
                                         .put("username", username)
                                         .put("permissions", permissions),
                                         new JWTOptions()
                                           .setSubject("Wiki API")
                                           .setIssuer("Vert.x"));
    return Response.ok(jwtToken).build();
  });
}
{% endhighlight %}

### OpenShift

Redpipe projects are very easy to deploy to [OpenShift V3](https://www.openshift.com), using the 
[Source-to-Image builders](https://docs.openshift.com/online/using_images/s2i_images/java.html)
and fat jars.

You can [see a sample Hello World application for yourself](https://github.com/FroMage/redpipe-openshift-helloworld),
 but the main idea is to set up
your main class so that it runs on the `8080` port:

{% highlight java %}
public class Main {
    public static void main( String[] args ){
        new Server()
        .start(new JsonObject().put("http_port", 8080), HelloResource.class)
        .subscribe(
                v -> System.err.println("Server started"),
                x -> x.printStackTrace());
    }
}
{% endhighlight %}

And then configure your `pom.xml` so that it creates a fat-jar with the proper merging of
`META-INF/services` files, and pointing to your `Main` class:

{% highlight xml %}
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fr.epardaud</groupId>
    <artifactId>redpipe-openshift-helloworld</artifactId>
    <version>{{page.version}}</version>
    <packaging>jar</packaging>

    <name>redpipe-openshift-helloworld</name>
    <url>http://maven.apache.org</url>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>net.redpipe</groupId>
            <artifactId>redpipe-engine</artifactId>
            <version>{{page.version}}</version>
        </dependency>

    </dependencies>

    <profiles>
        <profile>
            <id>fat-jar</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <build>
                <finalName>redpipe-helloworld</finalName>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-shade-plugin</artifactId>
                        <executions>
                            <execution>
                                <phase>package</phase>
                                <goals>
                                    <goal>shade</goal>
                                </goals>
                                <configuration>
                                    <transformers>
                                        <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer" />
                                    </transformers>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-jar-plugin</artifactId>
                        <configuration>
                            <archive>
                                <manifest>
                                    <mainClass>fr.epardaud.redpipe_openshift_helloworld.Main</mainClass>
                                </manifest>
                            </archive>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
        <profile>
            <id>flat-classpath-jar</id>
            <build>
                <finalName>redpipe-helloworld</finalName>
                <plugins>
                    <plugin>
                        <artifactId>maven-dependency-plugin</artifactId>
                        <executions>
                            <execution>
                                <phase>generate-sources</phase>
                                <goals>
                                    <goal>copy-dependencies</goal>
                                </goals>
                                <configuration>
                                    <outputDirectory>${project.build.directory}/lib</outputDirectory>
                                    <useRepositoryLayout>true</useRepositoryLayout>
                                </configuration>
                            </execution>
                            <execution>
                                <id>build-classpath</id>
                                <phase>generate-resources</phase>
                                <goals>
                                    <goal>build-classpath</goal>
                                </goals>
                                <configuration>
                                    <outputFile>${project.build.directory}/lib/classpath</outputFile>
                                    <localRepoProperty>lib</localRepoProperty>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>

</project>
{% endhighlight %}

Then, just follow the [official guidelines](https://docs.openshift.com/online/using_images/s2i_images/java.html) by
pushing your code to GitHub and starting an image with it. You can even try our sample app at 
`https://github.com/FroMage/redpipe-openshift-helloworld.git` (no context dir).

### Run your WebApp with Maven

This is a `pom.xml` sample to easily run your project. Just run `mvn install exec:java`

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>demo</groupId>
    <artifactId>demo</artifactId>
    <name>demo</name>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <redpipe.version>{{page.version}}</redpipe.version>
        <mainClass>demo.Main</mainClass>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>1.6.0</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>java</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <mainClass>${mainClass}</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>net.redpipe</groupId>
            <artifactId>redpipe-engine</artifactId>
            <version>${redpipe.version}</version>
        </dependency>
    </dependencies>
    
</project>
{% endhighlight %}
