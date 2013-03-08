define(["dojo/_base/declare", "ppwcode/contracts/_Mixin",
        "dojo/store/util/QueryResults", "dojo/store/util/SimpleQueryEngine",
        "dojo/store/Observable" /*=====, "./api/Store" =====*/],
  function(declare, _ContractsMixin,
           QueryResults, SimpleQueryEngine,
           Observable) {

    var ERROR_ALREADY_IN_STORE = new Error("Object already exists in this store");

    var OurStore = declare([_ContractsMixin], {
      // summary:
      //		An in-memory Observable object store like dojo/store/Memory wrapped in dojo/store/Observable,
      //    but with the following differences:
      //    1) We don't change the objects that are kept in the store. If you are not very
      //       careful, dojo/store/Memory inserts an "id" property for its own benefit,
      //       whose behavior depends on chance (random).
      //    2) We are aware of Stateful.
      //       If an element changes, we also send changed events. You do not have
      //       to re-put.
      //    3) removeAll, loadAll
      //    4) You need to inject a getIdentity function. It must work on all objects
      //       that ever were in the store or will be in the store, independent of the store.
      // description:
      //    On development we immediately need constructor, put, removeAll, loadAll, and query.
      //    getIdentity is needed for Observable.
      //    get and remove are optional.
      //    Query needs an array to be supplied. It therefor is wasteful to keep an index, which
      //    only helps with get and remove.
      //    idProperty is wasteful and stupid. We kill it.
      //    We wrap the real objects in a helper object, to keep track of the watchers of
      //    Stateful objects.
      //    If getIdentity changes, all bets are off.
      //
      //    Before you throw away the store, you must call removeAll, so that
      //    we can clean up listeners. Otherwise, you will have a memory leak.
      //
      //    Based on dojo/store/Memory

      _c_invar: [
        function() {return this._c_prop_mandatory("idProperty");},
        function() {return this._c_prop_string("idProperty");},
        function() {return this._c_prop_mandatory("idPropertyDerivation");},
        function() {return this._c_prop_function("idPropertyDerivation");}
      ],

      // _data: Array
      //   The internal representation of the data
      _data: null,

      // idPropertyDerivation: Function
      //    Stateful --> String
      //    Derives a unique id from a Stateful object as argument.
      getIdentity: null,

      // queryEngine: Function
      //		Defines the query engine to use for querying the data store
      queryEngine: SimpleQueryEngine,

      _wrap: function(/*Stateful*/ s) {
        var thisStore = this;

        function addWatcher(wrapper) {
          var watcher = function(name, oldValue, newValue) {
            // on change, see if the id has changed
            // if it has, signal removal, and a new addition
            // if is has not, signal change
            var oldId = wrapper.id;
            var newId = thisStore.getIdentity(wrapper.data);
            if (oldId != newId) {
              wrapper.id = newId;
              thisStore.notify(null, oldId);
              thisStore.notify(wrapper.data, null);
            }
            else {
              thisStore.notify(wrapper.data, oldId);
            }
          };
          wrapper.watcher = wrapper.data.watch(watcher);
        }

        var result = {
          id: thisStore.getIdentity(s),
          data: s
        };
        addWatcher(result);
        return result;
      },

      constructor: function(options){
        // summary:
        //		Creates a store of stateful objects.
        // options: dojo/store/Memory
        //		This provides any configuration information that will be mixed into the store.
        //		This should generally include the data property to provide the starting set of data.

        this._c_pre(function() {return options;});
        this._c_pre(function() {return this._c_prop_mandatory(options, "getIdentity");});

        var thisStore = this;
        Object.keys(options).
          filter(function(key) {return key !== "data";}).
          forEach(function(key) {thisStore[key] = options[key];});
        if (options && options.data) {
          thisStore._data = options.data.reduce(
            function(acc, element) {
              acc.push(thisStore._wrap(element));
              return acc;
            },
            []
          );
        }
        else {
          thisStore._data = [];
        }
      },

      contains: function(object){
        // summary:
        //		Is the object in this store?
        // object: Object
        //		The object to check membership for. We compare
        // returns: Boolean

        var filterResult = this._data.filter(function(wrapper) {
          return wrapper.data === object;
        });
        if (filterResult.length > 1) {
          throw "ERROR: object in store more then once (object: " + object + ", store: " + this + ")";
        }
        else if (filterResult.length === 0) {
          return null;
        }
        else {
          return filterResult[0][this.idProperty];
        }
      },

      get: function(id){
        // summary:
        //		Retrieves an object by its identity
        // id: Number
        //		The identity to use to lookup the object
        // returns: Object
        //		The object in the store that matches the given id.

        var filterResult = this._data.filter(function(wrapper) {
          return wrapper.id === id;
        });
        if (filterResult.length > 1) {
          throw "ERROR: duplicate id in store (id: " + id + ", store: " + this + ")";
        }
        else if (filterResult.length === 0) {
          return null;
        }
        else {
          return filterResult[0].data;
        }
      },

      put: function(object) {
        // summary:
        //		Stores an object. Options are ignored.
        // object: Object
        //		The object to store.
        // returns: Number

        try {
          this.add(object);
        }
        catch (e) {
          if (e !== ERROR_ALREADY_IN_STORE) {
            throw e;
          }
        }
      },

      add: function(object){
        // summary:
        //		Creates an object, throws an error if the object already exists
        //		Options are ignored.
        // object: Object
        //		The object to store.
        // returns: Number

        if (this.get(this.getIdentity(object))) { // we have this object already
          throw ERROR_ALREADY_IN_STORE;
        }
        var wrapper = this._wrap(object);
        this._data.push(wrapper); // Store spec doesn't say what we should return. We return nothing.
      },

      remove: function(id){
        // summary:
        //		Deletes an object by its identity
        // id: Number
        //		The identity to use to delete the object
        // returns: Boolean
        //		Returns true if an object was removed, falsy (undefined) if no object matched the id

        var thisStore = this;
        var foundAtIndex = this._data.reduce(
          function(acc, wrapper, index) {
            if (wrapper.id === id) {
              if (acc > -1) {
                throw "ERROR: duplicate id in store (id: " + id + ", store: " + thisStore + ")";
              }
              else {
                wrapper.watcher.unwatch();
                return index;
              }
            }
            else {
              return acc;
            }
          },
          -1
        );
        if (foundAtIndex > -1) {
          thisStore._data.splice(foundAtIndex, 1);
        }
        // Store spec doesn't say what we should return. We return nothing.
      },

      query: function(query, options){
        // summary:
        //		Queries the store for objects.
        // query: Object
        //		The query to use for retrieving objects from the store.
        // options: dojo/store/api/Store.QueryOptions?
        //		The optional arguments to apply to the resultset.
        // returns: dojo/store/api/Store.QueryResults
        //		The results of the query, extended with iterative methods.
        //
        // example:
        //		Given the following store:
        //
        // 	|	var store = new Memory({
        // 	|		data: [
        // 	|			{id: 1, name: "one", prime: false },
        //	|			{id: 2, name: "two", even: true, prime: true},
        //	|			{id: 3, name: "three", prime: true},
        //	|			{id: 4, name: "four", even: true, prime: false},
        //	|			{id: 5, name: "five", prime: true}
        //	|		]
        //	|	});
        //
        //	...find all items where "prime" is true:
        //
        //	|	var results = store.query({ prime: true });
        //
        //	...or find all items where "even" is true:
        //
        //	|	var results = store.query({ even: true });
        return QueryResults(this.queryEngine(query, options)(this._data.map(function(wrapper) {
          return wrapper.data;
        })));
      },

      loadAll: function(data) {
        // summary:
        //   replaces current data with new data; common objects
        //   are not signalled as removed and added again

        var thisStore = this;
        var inData = data.slice(0);
        thisStore._data = thisStore._data.reduce(
          function (acc, wrapper) {
            var indexInData = inData.indexOf(wrapper.data);
            if (indexInData < 0) {
              // not in inData; don't add to acc, signal removal
              wrapper.watcher.unwatch();
              thisStore.notify(null, wrapper.id);
            }
            else {
              // keep the element (add to acc), and note handled.
              inData.splice(indexInData, 1);
              acc.push(wrapper);
            }
            return acc;
          },
          []
        );
        // what is left in inData needs to be added
        inData.forEach(function(newElement) {
          thisStore.put(newElement);
        });
      },

      removeAll: function() {
        var thisStore = this;

        var oldData = thisStore._data;
        thisStore._data = [];
        oldData.forEach(function(wrapper) {
          wrapper.watcher.unwatch();
          thisStore.notify(null, wrapper.id);
        });
      },

      getLength: function() {
        return this._data.length;
      }

    });

    var OurObservableStore = function(options) {
      return Observable(new OurStore(options));
    };

    return OurObservableStore;

  });