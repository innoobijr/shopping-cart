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
Kleisli[OptionT[F, ?], Request, Response] 

// If we modify Request and Response types we get
Kleisli[OptionT[F, ?], Request[F], Response[F]] 

//which is
type HttpRoutes[F] = Kleisli[OptionT[F, ?], Request[F], Response[F]] 

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
type HttpRoutes[F[_]] = Http[OptionT[F, ?], F]

```

#### 2. Composing Routes
Use `SemigroupK` which is included in `cats.core`. It is very similar to `Semigroup`, the different 
is that `SemigroupK` operates on type constructors of one argument i.e `F[_]`. 