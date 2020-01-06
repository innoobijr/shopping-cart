# Design Bits

#### 1. A Server is a Function
A simple HTTP server can be represented with the following function:
```scala
Request => Response
```

However, we commonly need to perform ane effectful operation such as retrieving data from
a database, before returing a response, so we need something more like:
```scala
Request => F[Response]
```

In order to compose routes, we need to model the possibility that not every single request will
have a response, so we can iteratre over the list of routes and try to match the nexct one. When
we reach the end, we give up and return a default response, mroe likely a `404 Not Found`. For such 
case, we need a type that lets us express the optionality:
```scala
Request => F[Option[Response]]

//or as a Monad Transformer
Request => OptionT[F, Response]

//or finally, Kleisli, or also know as ReaderT, is a monad transformer for functins, we can replace the 
// => arrow with it:
Kleisli[OptionT[F, *], Request, Response] 

// If we modify Request and Response types we get
Kleisli[OptionT[F, *], Request[F], Response[F]] 

//which is
type HttpRoutes[F] = Kleisli[OptionT[F, *], Request[F], Response[F]] 

```
There are cases where we need to guarantee tha given a request we can retuen a response. In such cases
we need ot remove the optionality:
```scala
Kleisli[F, Request[F], Response[F]] 
type HttpApp[F] = Kleisli[F, Request[F], Response[F]] 
```
```scala
//Further generalizing
type Http[F[_], G[_]] = Kleisli[F, Request[G], Response[G]]

type HttpApp[F[_]] = Http[F, F]
type HttpRoutes[F[_]] = Http[OptionT[F, *], F]

```

#### 2. Composing Routes
Use `SemigroupK` which is included in `cats.core`. It is very similar to `Semigroup`, the different 
is that `SemigroupK` operates on type constructors of one argument i.e `F[_]`. 


#### 3. Middlewares
Middleware allows us ot manipulate `Requests` and `Responses`. it is expressed as a function.
The two most common middlewares have either of the following shapes:
```scala
HttpsRoutes[F] => HttpRoutes[F]
```
or 
```scala
HttpsApp[F] => HttpApp[F]
```

It definition is more generic:
```scala
type Middleware[F[_], A, B, C, D] = Kleisli[F, A, B] => Kleisli[F, C, D]

```

There are a few predefined middlewate we can make use of, i.e CORS middleware, If we wanted 
to support CORS for all our routes, we could do the following:
```scala
val modRoutes: HttpRoutes[F] = CORS(allRoutes)
```

##### Compositionality
Since middlewares are functions, we can define a single function that combines all the
middlewares we want to apply to all our HTTP routes. Here is one simple way to do it:
```scala
val middleware: HttpRoutes[F] => HttpRoutes[F] = {
{ http: HttpRoutes[F] =>
  AutoSlash(http)
} andThen { http: HttpRoutes[F] =>
  CORS(http, CORS.DefaultCORSConfig)
} andThen { http: HttpRoutes[F] =>
  Timeout(60.seconds)(http)
}
```
Some middlewares require an HttpApp[F] instead of HttpRoutes[F], in such a case, it
is better to declare them separately:
```scala
val closedMiddleware: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(true, true)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(true, true)(http)
    }
}
```
Then, we can compose them together as follows:

```scala
val finalRoutes: HttpApp[F] =
    closedMiddleware(middleware(allRoutes).orNotFound)
```

#### 3. Using Skunk
* Once we have the schema, we need ot define the codes, querires, and commands. A good
practivec is to define thatn in a private object in the same file. 