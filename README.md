# ldnclj-datomic

I was disappointed with how confusing it seemed to get started with Datomic in my first meetup, so I had a think about it and realised that we could greatly simplify the server implementation by hooking Datomic up to core.async in the client.  The example page shows inserting a user, an event, adding the user to the list of atendees, and then using a pull query to get back the data.  It's the pull query that is most interesting - it allows components to ask for exactly the data they need, instead of us having to implement an API to do this.  Changing the domain is now just a matter of changing the Datomic schema and the clojurescript that inserts/displays it.

Things worth mentioning:

* I implemented startup/shutdown using yoyo, thinking that was on the trello board, when in fact it was yada...
* The client is as simple as I could make it so as not to distract - plain cljs/html
* I won't get all wobbly-bottom-lip if we don't use it - this was useful to me on a side project
* There's no security.  EDN-injection protection would be implemented by examining the data passed to insert to see if the current user is allowed to do that, and queries would be filtered for allowed data.  Just an implementation detail...

## Usage

```
$ lein repl
nREPL server started on port 51539 on host 127.0.0.1 - nrepl://127.0.0.1:51539
user=> (require '[meetdown.core])
nil
user=> (in-ns 'meetdown.core)
#object[clojure.lang.Namespace 0x3a6cb9eb "meetdown.core"]
meetdown.core=> (create-dev-system)
#object[meetdown.core$create_dev_system$fn__15391 0x535dd914 "meetdown.core$create_dev_system$fn__15391@535dd914"]
meetdown.core=> (y/start!)
meetdown.core=> (y/stop!) <-- shut down after you're finished
```

Then browse to http://localhost:8090/

## License

Copyright © 2015 p14n

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
