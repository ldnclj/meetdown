# ldnclj-datomic

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

   * Add RESTful support for HTTP methods in compjure routes and
   entity end points
   * Add validation - e.g. schema
   * Add security - TBD
   * Example client is broken but I suggest we don't spend time on it
     and just wire in the client from proclodo-reagent-spike and join
     the two as a new project once they are in a stable appropriate point.

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

### Dean's thoughts


I was disappointed with how confusing it seemed to get started with Datomic in my first meetup, so I had a think about it and realised that we could greatly simplify the server implementation by hooking Datomic up to core.async in the client.  The example page shows inserting a user, an event, adding the user to the list of atendees, and then using a pull query to get back the data.  It's the pull query that is most interesting - it allows components to ask for exactly the data they need, instead of us having to implement an API to do this.  Changing the domain is now just a matter of changing the Datomic schema and the clojurescript that inserts/displays it.

Things worth mentioning:

* I implemented startup/shutdown using yoyo, thinking that was on the trello board, when in fact it was yada...
* The client is as simple as I could make it so as not to distract - plain cljs/html
* I won't get all wobbly-bottom-lip if we don't use it - this was useful to me on a side project
* There's no security.  EDN-injection protection would be implemented by examining the data passed to insert to see if the current user is allowed to do that, and queries would be filtered for allowed data.  Just an implementation detail...

### Stu's two cents

I had a quick look at integration testing the system from the REST layer downwards, but went a little off the rails.
Sorry!

I struggled a bit due to unfamiliarity with Yo-yo. It seemed that the best way to test was to use
yoyo.core/with-component, but to do so I needed to refactor the code to expose the Yo-yo components directly. I did
that, and although it ended up a little boilerplate-y it all worked. However, in the process of getting it working, I
found that the author of Yo-yo has decided to abandon the project:

    I'm in the process of abandoning Yo-yo - while it was a good experiment, it seems in practice that this isn't an
    easy way to write readable code, and that refactoring Yo-yo based code is more difficult than 'normal' Clojure.

Augh! What to do? I struggled with keeping it (and increasing the amount of Yo-yo in the project which might be a bad
thing if you now wanted to remove it) versus removing it (and taking out something you guys plumbed in and might want to
keep using despite the author's decision).

In the end I thought I'd try taking out Yo-yo and replacing it with something like Stuart Sierra's Reloaded structure.
Apologies to Dean and everyone who put in the Yo-yo code -- and I'm not at all wedded to any of this so feel free to
rewrite/discard/slash/burn as you see fit. I do still have the Yo-yo version somewhere so I can push that if you still
want to use Yo-yo -- I'm totally happy with that, too. It looks interesting, and the only thing that put me off it a
little was the author's decision.

As for the tests, they're in meetdown.http-test and just test the REST service by executing the router. I haven't looked
at using other testing frameworks (jaycfields/expectations was mentioned, and yeller/matcha) because I don't want to
monopolise and looking at those seemed like a nice task for someone else to play with.

Questions I've got

* Should I stick Yoyo back in? Help! I feel bad for taking it out!
* I wondered what the REST API should look like? At the moment it's just one post method for creating items and for
  getting them. Do we care about making this look like a regular REST API if it's only going to be used by our
  ClojureScript client?
* Would it be worth defining the API? If we do that, we can write tests and TDD the server development, and it'd give
  the guys working on the client a foundation they can work on. Thoughts?
  * Having read the status, it sounds like we're wanting to move to more of a standard REST API. How about something
    like this, and if we're happy with it, how about we put this in during the next dojo?
    * POST /users => Create a user, return ID and CREATED
    * GET /user/ID => Return resource for user with ID or NOT_FOUND
    * POST /events => Create an event, return ID and CREATED
    * GET /event/ID => Return resource for event with ID or NOT_FOUND
    * PATCH /event/ID => Add speakers and/or attendees to event?

## Usage

```

$ lein repl
nREPL server started on port 38344 on host 127.0.0.1 - nrepl://127.0.0.1:38344
REPL-y 0.3.5, nREPL 0.2.12
Clojure 1.7.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_60-b27
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

meetdown.core=> (-main)
15-Dec-12 13:01:22 stuw-ThinkPad-X240 INFO [meetdown.data] - Starting database
15-Dec-12 13:01:22 stuw-ThinkPad-X240 INFO [meetdown.http] - Starting http server
{:config {:dburi "datomic:mem://meetdown", :http-kit {:port 3000}}, :db-conn #object[datomic.peer.LocalConnection 0x5ca0254f "datomic.peer.LocalConnection@5ca0254f"], :f-stop-http! #object[clojure.lang.AFunction$1 0x51084c07 "clojure.lang.AFunction$1@51084c07"], :handler #object[ring.middleware.x_headers$wrap_content_type_options$fn__11876 0x1720442c "ring.middleware.x_headers$wrap_content_type_options$fn__11876@1720442c"]}
meetdown.core=> 
```

Then browse to http://localhost:3000/

## License

Copyright © 2015 p14n

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
