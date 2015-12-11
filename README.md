# meetdown
Event management system

## Status

With apologies to Dean the team felt that the server should not
directly expose Datomic queries so that it's easier to wrap security
and validation around the requests.

Currently the client still sends the type of 'query'
i.e. :create-event, :get-events. All 'queries' are sent as POST HTTP
requests.

The concensus is to move towards using a more RESTful approach with
the HTTP method and resource end points to indicate the required data
action.

TODO

   * Add validation - e.g. schema
   * Add security - TBD


### CHJ's thoughts - YMMV

I like the idea of having the only changes to support changes in the
domain model being at the database and the client. However, I believe
that good data validation at the server end point catches a whole
category of bugs and is a major security feature (defence against DoS
and 'injection' style attacks). Therefore I think we need to harden the API on the
server by adding validation.

I also think that supporting a more traditional RESTful interface
makes it easier for new members of the team (and this team is super
fluid!) to grasp the concepts.

_However_ this is not my project. It's a community project. Although I
don't want to be paralysed by constant switching of approaches I will
take arguments to the contrary and am happy to be persuaded
otherwise. For me, the key principle I won't be moved on is
inclusivity. We need to keep the project accessible to as many as possible.

## Usage

```
$ lein repl
nREPL server started on port 53910 on host 127.0.0.1 - nrepl://127.0.0.1:53910
...
user=> (go)
datomic:mem://meetdown
Starting Datomic connection for  datomic:mem://meetdown
Starting handler routes
Starting http-kit
#<SystemMap>
...
user=> (reset) ; restart the system
Shutting down http-kit
:reloading (meetdown.core meetdown.data meetdown.http meetdown.core-test user)
datomic:mem://meetdown
Starting Datomic connection for  datomic:mem://meetdown
Starting handler routes
Starting http-kit
#<SystemMap>
...
user=> (stop) ; shutdown
Shutting down http-kit
#<SystemMap>

user=>
```

You can use the following curl commands from a terminal session to
send requests to the server.

To create a new event:

```
$ curl -X POST -d "{:type :create-event :txn-data {:event/name \"New event\"}}" http://localhost:3000/q --header "Content-Type:application/edn"
17592186045420
```

To retrieve all events:

```
$ curl -X POST -d "{:type :get-events}" http://localhost:3000/q --header "Content-Type:application/edn"
[{:db/id 17592186045420, :event/name "New event"}]
$
```

## License

Copyright © 2015 London Clojurians

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
