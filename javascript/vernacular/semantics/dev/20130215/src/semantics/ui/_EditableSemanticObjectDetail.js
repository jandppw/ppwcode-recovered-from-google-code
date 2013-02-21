define(["dojo/_base/declare", "ppwcode/contracts/_Mixin", "dojo/_base/lang", "dijit/registry", "dojo/dom-style", "dojo/dom-class", "dojo/has",
        "dijit/_WidgetBase", "../SemanticObject",
        "ppwcode/contracts/doh"], // MUDO REMOVE temp for testing invariants in the field
    function(declare, _ContractMixin, lang, registry, domStyle, domClass, has,
             _WidgetBase, SemanticObject,
             doh) {

      var editModes = [
        // editMode for when there is no target
        "NOTARGET",

        // editMode for viewing the data. No interaction allowed.
        "VIEW",

        // editMode for editing the data. Interaction allowed.
        "EDIT",

        // editMode while the SemanticObject is busy. No interaction allowed.
        "BUSY",

        // editMode representing unacceptable data (the object is not civilized,
        // or we have other means of knowing the data is unacceptable).
        // Interaction must be allowed to reset the object, or ammeliorate the data/
        // This means this is a sub-state of EDIT.
        "WILD",

        // editMode representing an error occured. We cannot proceed. The widget
        // and its object must be destroyed.
        "ERROR"];

      function setEditModeOnWidgets(domNode, editMode) {
        if (has("dojo-debug-messages")) {
          // make ERROR and WILD mode very clear in debug mode
          if (editMode === _EditableSemanticObjectDetail.prototype.NOTARGET) {
            domStyle.set(domNode, "backgroundColor", "gray");
          }
          else if (editMode === _EditableSemanticObjectDetail.prototype.ERROR) {
            domStyle.set(domNode, "backgroundColor", "red");
          }
          else if (editMode === _EditableSemanticObjectDetail.prototype.WILD) {
            domStyle.set(domNode, "backgroundColor", "yellow");
          }
          else {
            domStyle.set(domNode, "backgroundColor", "");
          }
        }
        var innerWidgets = registry.findWidgets(domNode);
        var widgetState = null;
        switch (editMode) {
          case _EditableSemanticObjectDetail.prototype.NOTARGET:
            widgetState = { readOnly:false, disabled:true };
            break;
          case _EditableSemanticObjectDetail.prototype.EDIT:
            widgetState = { readOnly:false, disabled:false };
            break;
          case _EditableSemanticObjectDetail.prototype.BUSY:
            widgetState = { readOnly:false, disabled:true };
            break;
          case _EditableSemanticObjectDetail.prototype.ERROR:
            widgetState = { readOnly:false, disabled:true };
            break;
          default:
            widgetState = { readOnly:true, disabled:false };
        }
        innerWidgets.forEach(function (w) {
          if (!w.isInstanceOf(_EditableSemanticObjectDetail)) {
            w.set("readOnly", widgetState.readOnly);
            w.set("disabled", widgetState.disabled);
          }
        });
      }

      function setEditModeOnBlock(domNode, editMode) {
        if (editMode === _EditableSemanticObjectDetail.prototype.VIEW ||
            editMode === _EditableSemanticObjectDetail.prototype.EDIT ||
            editMode === _EditableSemanticObjectDetail.prototype.WILD) {
          domClass.replace(domNode,
            "editableSemanticObjectDetail_blockEnabled",
            "editableSemanticObjectDetail_blockDisabled editableSemanticObjectDetail_blockInvisible"
          );
        }
        else if (editMode === _EditableSemanticObjectDetail.prototype.BUSY ||
                 editMode === _EditableSemanticObjectDetail.prototype.ERROR) {
          domClass.replace(domNode,
            "editableSemanticObjectDetail_blockDisabled",
            "editableSemanticObjectDetail_blockEnabled editableSemanticObjectDetail_blockInvisible"
          );
        }
        else { // no target
          domClass.replace(domNode,
            "editableSemanticObjectDetail_blockInvisible",
            "editableSemanticObjectDetail_blockEnabled editableSemanticObjectDetail_blockDisabled"
          );
        }
      }

      var _EditableSemanticObjectDetail = declare([_WidgetBase, _ContractMixin], {
        // summary:
        //   Widget that represents a SemanticObject in detail, and that gives the opportunity
        //   to the user the view the details, edit the details, and create a new object.
        //   Abstract class to be extended for particular subtypes of SemanticObject.
        // description:
        //   What is common to all subtypes is that the widget:
        //   - has a `target` that is a SemanticObject; getTargetType() returns the top-most supported
        //     Constructor of supported targets. All targets must be instances of this type.
        //   - has an `editMode` (view, edit, busy, wild, error)
        //
        //   The representation should reflect the editMode in a clear visual way to the user.
        //   Even when the information shown is completely read-only, widgets should extend
        //   this class, to change the representation of the displayed information consistently
        //   with other information in different editModes, e.g., in "busy" mode.
        //   The available edit modes are defined in _EditableSemanticObjectDetail.prototype.editModes.
        //
        //   Instances can be wrapped around zero or more _EditableSemanticObjectDetails, recursively (no loops!).
        //   We take care of propagating `editMode`. Subclasses need to override
        //   getWrappedDetails to return the wrapped details. All instances need to be instances of this class.
        //   The default implementation returns an empty array.
        //   The target of a wrapped detail might be the same target as our target, but it might be
        //   another related object, of a different type. set("target", ...) will call _propagateTarget, which is
        //   chained, so that subclasses can add propagation of setting the target. Our implementation only sets our
        //   target.

        _c_invar: [
          function() {return this.get("editMode");},
          function() {return _EditableSemanticObjectDetail.prototype.editModes.indexOf(this.get("editMode")) >= 0;},
          function() {return this.getTargetType() != null;},
          // TODO getTargetType is a subtype of SemanticObject
          function() {return this.get("target") == null ||
                        (this.get("target").isInstanceOf && this.get("target").isInstanceOf(this.getTargetType()));},
          function() {return (this.get("editMode") === this.NOTARGET) === (this.get("target") === null);},
          function() {return this._wrappedDetails() != null;},
          function() {return lang.isArray(this._wrappedDetails());},
          {
            objectSelector: function() {
              return this._wrappedDetails();
            },
            invars: [
              function(wd) {
                return wd && wd != null &&
                  wd.isInstanceOf && wd.isInstanceOf(_EditableSemanticObjectDetail);
              },
              // wrapped details might contain other objects of other types as target,
              // but the editMode needs to be in sync
              function(wd) {
                return wd.get("editMode") === this.get("editMode");
              }
            ]
          }
        ],

        "-chains-": {
          _propagateTarget: "after",
          _localEditModeChange: "after"
        },

        // target: SemanticObject
        //    SemanticObject that is represented.
        //    Access in declarative template with  ... data-dojo-props="value: at('rel:', BINDING_PROPERTY_NAME)
        target: null,

        // editMode: String
        //    The edit mode is either
        editMode: editModes[0], // default value

        isInEditMode: function() {
          return this.editMode === this.EDIT || this.editMode === this.BUSY || this.editMode === this.WILD;
        },

        _wrappedDetails: function() {
          // summary:
          //   Array containing the wrapped details, a subclass wants the target and editMode propagated to.
          // tags:
          //   protected

          return []; // return Array
        },

        getTargetType: function() {
          // summary:
          //    The supported type of SemanticObjects. All targets must be an instance of this type.
          // description:
          //    Subclasses should overwrite to return the correct type.

          return SemanticObject;
        },

        _propagateTarget: function(/*SemanticObject*/ so) {
          // summary:
          //   Propagate the target as appropriate to wrapped details.
          //   Chained. Subtypes could add propagation to wrapped details.
          // tags:
          //   protected
          // description:
          //   Does nothing in _EditableSemanticObjectDetail

          this._c_NOP();
        },

        _setTargetAttr: function(so) {
          // summary:
          //    Sets the target of this instance. Chained. Subtypes could add propagation to wrapped details.
          this._c_pre(function() {return so == null || (so.isInstanceOf && so.isInstanceOf(this.getTargetType()));});

          if (so !== this.get("target")) {
            this._set("target", so);
          }
          this._propagateTarget(so);
          this.set("editMode", so ? this.VIEW : this.NOTARGET);

          doh.validateInvariants(this); // MUDO REMOVE
        },

        _setEditModeAttr: function(value) {
          // summary:
          //    Set the editMode, and propagate to the wrapped details.
          //    Also makes all innerWidgets read-only in editMode VIEW,
          //    disabled in editMode BUSY, and not-read-only and enabled
          //    in the other editModes.

          // Called during create by _WidgetBase with default value automatically
          this._c_pre(function() {return value != null});
          this._c_pre(function() {return _EditableSemanticObjectDetail.prototype.editModes.indexOf(value) >= 0});

          this._set("editMode", value);
          this._wrappedDetails().forEach(function(wd) {
            wd.set("editMode", value);
          });
          setEditModeOnBlock(this.get("domNode"), value);
          setEditModeOnWidgets(this.get("domNode"), value);
          this._localEditModeChange(value);
          doh.validateInvariants(this); // MUDO REMOVE
        },

        _localEditModeChange: function(/*String*/ editMode) {
          // summary:
          //   Make changes to representation to represent the
          //   new edit mode. Chained. Overwrite when needed.
          // tags:
          //   protected
          // description:
          //   Does nothing in _EditableSemanticObjectDetail

          this._c_NOP();
        }

      });

      // All supported editModes
      _EditableSemanticObjectDetail.prototype.editModes = editModes;
      editModes.forEach(function(em) {
        _EditableSemanticObjectDetail.prototype[em] = em;
      });

      return _EditableSemanticObjectDetail; // return _EditableSemanticObjectDetail

    });