dojo.provide("org.ppwcode.dojo.dijit.form._FormDataViewBox");

dojo.require("org.ppwcode.dojo.dijit.form._DataViewBox");
dojo.require("dijit.form._FormWidget");

dojo.declare(
	"org.ppwcode.dojo.dijit.form._FormDataViewBox",
	[dijit.form._FormValueWidget, org.ppwcode.dojo.dijit.form._DataViewBox],
	{
		_selectable: true,
		_resizeeventhandle: null,
		
		postCreate: function() {
			this.inherited(arguments);
			this._overrideGridCanEdit();
			this._connectToParentContainer();
		},

		_overrideGridCanEdit: function() {
			//DataGrid's editable status depends on the Write capability
			//of its data store,  in our case, the write capability
			//also depends on the _selectabe variable.  so we do some nasty
			//stuff to 
			var oldCanEdit = this._masterGrid.canEdit;
			var f = dojo.hitch(this._masterGrid, oldCanEdit);
			var g = dojo.hitch(this, this.getSelectable);
			this._masterGrid.canEdit = function() {
				return (f() && g());
			};
		},
		
		_connectToParentContainer: function() {
			// summary:
			//   connect to a container up in the DOM tree so we can redraw
			//   ourselves upon resizing of the parent container
			// description:
			//   this is a hack :/... It's the only way I found to redraw
			//   the grid if resize events other than the window resize
			//   events occur.
			var potentialNode = this.domNode.parentNode;
			var found = false, nodesdijit = null;
			while (!found && potentialNode != dojo.body()) {
				nodesdijit = dijit.byNode(potentialNode);
				if ( nodesdijit && ( (nodesdijit instanceof dijit.layout._LayoutWidget) 
			        	             || (nodesdijit instanceof dijit.layout.ContentPane) )    
			       ) {
				  found = true;
				} else {
                  potentialNode = potentialNode.parentNode;
				}
			}
			if (found) {
				//console.log("_FormDataViewBox._connectToParentContainer(): potentialNode is " + potentialNode);
				//console.log("_FormDataViewBox._connectToParentContainer(): dijit is " + nodesdijit);
				this._resizeeventhandle = dojo.connect(nodesdijit, "resize", this, "layout");
			} // else {
			//	console.log("_FormDataViewBox._connectToParentContainer(): nothing found");
			//}
		},
		
		uninitialize: function() {
			if (this._resizeeventhandle) {
				dojo.disconnect(this._resizeeventhandle);
			}
			this.inherited(arguments);
		},

		setSelectable: function(/*Boolean*/isSelectable) {
			this._selectable = isSelectable;
			this._applySelectable(this._selectable);
		},
	
		getSelectable: function() {
			return this._selectable;
		},
		
		_applySelectable: function(/*Boolean*/isSelectable) {
			this._masterGrid.onCanSelect = function() {
				return isSelectable;
			}	
			this._masterGrid.onCanDeselect = function() {
				return isSelectable;
			}
		},

		clearSelection: function() {
			// summary:
			//   Clear the selection in the DataGrid.
			// description:
			//   Clears the selection in the Datagrid.  Calling this method
			//   also triggers the onClearSelection event method.
			if (this._selectable) {
				this.inherited(arguments);
			}
		},
		
		_clearSelection: function() {
			this._applySelectable(true);
			this.inherited(arguments);
			this._applySelectable(this._selectable);
		},

		_setDisabledAttr: function(value) {
			this.inherited(arguments);
			this.setSelectable(!value);
		},			
		
		_getDisabledAttr: function() {
			return this._selectable;
		},
		
		reset: function() {
			this.inherited(arguments);
			this._clearSelection();
		}
	}
);