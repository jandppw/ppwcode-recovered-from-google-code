package be.peopleware.jsf_II.persistence;


import java.util.Arrays;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletRequestListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.peopleware.bean_IV.CompoundPropertyException;
import be.peopleware.exception_I.TechnicalException;
import be.peopleware.jsf_II.FatalFacesException;
import be.peopleware.jsf_II.RobustCurrent;
import be.peopleware.persistence_I.IdNotFoundException;
import be.peopleware.persistence_I.PersistentBean;
import be.peopleware.persistence_I.dao.AsyncCrudDao;


/**
 * <p>Handler for {@link PersistentBean} detail CRUD pages.</p>
 * <p>This handler can be used in a number of circumstances. The main use
 *   is as the backing bean for a detail CRUD page of an instance of semantics.
 *   For this to work, the handler needs a {@link #getDao()} the
 *   {@link #getType()} filled out,
 *   needs to know the previous {@link #getViewMode()},
 *   and it needs an {@link #getInstance() instance}.</p>
 * <p>The instance can be set explicitly with {@link #setInstance(PersistentBean)}.
 *   This immediately also sets the {@link #getId()}. If the
 *   {@link #getInstance() instance} is <code>null</code> when it is requested,
 *   we will retrieve the instance with id {@link #getId()} and type
 *   {@link #getType()} from persistent storage, and cache it. If at this time
 *   {@link #getId()} is <code>null</code>, a new instance of {@link #getType()}
 *   will be created.</p>
 * <p>In conclusion this means that, before an instance of this class can be used,
 *   you need to set the<p>
 * <ul>
 *   <li>the {@link #setType(Class) type} and</li>
 *   <li>the {@link #setViewMode(String) previous view mode}, and</li>
 *   <li>either
 *     <ul>
 *       <li>an {@link #setInstance(PersistentBean) instance},</li>
 *       <li>or
 *         <ul>
 *           <li>a {@link #setDao(AsyncCrudDao) dao}, and</li>
 *           <li>an {@link #setId(Long) id}.</li>
 *         </ul>
 *       </li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>States &amp; Transitions</h2>
 * <p>An <code>PersistentBeanCrudHandler</code> has 4 states and
 *  a number of state transitions. The states are actually <dfn>view modes</dfn>
 *  for the JSF page.</p>
 * <img src="doc-files/persistence.gif" style="width: 100%;" />
 *
 * <h3>Retrieving the {@link be.peopleware.persistence_I.PersistentBean Instance}</h3>
 * <p>With each HTTP request (except the request for a screen to fill out the
 *  values for a new instance, the request to create a new instance, the request
 *  to show a list of persistent beans and a request to go back to the
 *  previous page) we expect the
 *  {@link be.peopleware.persistence_I.PersistentBean#getId() primary key of a
 *  persistent bean} as request parameter. This id is filled in in the handler's
 *  {@link #getId()} property. Before
 *  the Update Model Values phase is reached, we need to load the
 *  {@link be.peopleware.persistence_I.PersistentBean} with
 *  {@link #getId() this id} and type {@link #getType()} from
 *  persistent storage. This instance will be stored in the
 *  {@link #getInstance() instance handler property}.</p>
 * <p>It is always possible that no instance with {@link
 *  #getId() this id} and  {@link #getType() type} can be found
 *  in persistent storage, because it never existed, or because such instance
 *  was removed in-between user requests from persistent storage. Whenever this
 *  occurs, the user will be directed back to the page he originally came from,
 *  before he accessed this page.</p>
 * <p>Again it is possible that the previous instance does not exist in persistent
 *  storage anymore, so this process is recursive, until the first page after
 *  login is reached.</p>
 * <p>If the user itself is removed from the system while logged in, the session
 *  will be stopped as early as possible, and the user will return to the login
 *  page.</p>
 * <p>HOW IS THIS ACHIEVED?</p>
 * <h3>Entry</h3>
 * <p>The state machine is entered by a navigation request (<code>navigate(id)
 *  [found(id)]</code>), or a request to edit a new instance
 *  (<code>editNew()</code>).</p>
 * <p>Entry is requested always from the handler of a previous page. When entry
 *  succeeds, the previous page is recorded in the  TODO NavigationStack, so we
 *  can go back to it later.</p>
 * <h3>Display</h3>
 * <p>In view mode <code>display</code>, the data of the {@link
 *  be.peopleware.persistence_I.PersistentBean} is shown, in a non-editable
 *  way.</p>
 * <h4>delete</h4>
 * <p>From this state, we can try to delete the {@link
 *  be.peopleware.persistence_I.PersistentBean}. When the bean is not found in
 *  persistent storage (<code>delete(id) [! found(id)]</code>), the user is
 *  brought back to the previous page, and a message is shown. If the bean is
 *  found in persistent storage, deletion might fail for semantic reasons
 *  (<code>delete(id) [found(id) &amp;&amp; exception]</code>). In this case, we
 *  stay in <code>display</code> state, and show a message to the user about the
 *  exception. When deletion succeeds (<code>delete(id) [found(id) &amp;&amp;
 *  nominal] / DELETE</code>), the instance is deleted from persistent storage,
 *  and the handler goes to the <code>deleted</code> state.</p>
 * <p>This is implemented in the action method {@link #delete()}. The user can
 *  request the deletion by clicking a button defined as:</p>
 * <pre>
 *   &lt;h:commandButton action=&quot;#{<var>myHandler</var>.delete}&quot;
 *                    value=&quot;#{<var>myHandler</var>.buttonLabels['delete']}&quot;
 *                    rendered=&quot;#{<var>myHandler</var>.viewMode eq 'display'}&quot;
 *                    immediate=&quot;true&quot;/&gt;
 * </pre>
 * <h4>edit</h4>
 * <p>In <code>display</code> mode, the user can also ask for <code>edit</code> mode. When the
 *  bean is not found in persistent storage (<code>edit(id) [! found(id)]</code>),
 *  the user is brought back to the previous page, and a message is shown. If the
 *  bean is found in persistent storage (<code>edit(id) [found(id)]</code>), the
 *  handler goes to the <code>edit</code> state.</p>
 * <p>This is implemented in action method {@link #edit()}. The user can request
 *  view mode <code>edit</code> by clicking a button defined as:</p>
 * <pre>
 *   &lt;h:commandButton action=&quot;#{<var>myHandler</var>.edit}&quot;
 *                    value=&quot;#{<var>myHandler</var>.buttonLabels['edit']}&quot;
 *                    rendered=&quot;#{<var>myHandler</var>.viewMode eq 'display'}&quot;
 *                    immediate=&quot;true&quot;/&gt;
 * </pre>
 * <h4>goBack</h4>
 * <p>From display mode, the user can request to go back to the previous
 *  page (<code>goBack()</code>). If the previous instance cannot be found, this
 *  action propagates the user to instances visited earlier, recursively.</p>
 * <p>HOW IS THIS IMPLEMENTED?  The user can request to go back by clicking a
 *  button defined as:</p>
 * <pre>
 *  MUDO (jand) NO IDEA YET
 * </pre>
 * <h4>navigate</h4>
 * <p>Finally, the user can request to navigate to another page
 *  (<code>nextPageHandler.navigate(nextPageId) [found(nextPageId)]</code>).
 *  Presumably, he clicks on a link to navigate to a related bean. This state
 *  transition is a placeholder for any number of possible navigation points in
 *  the page. The intention is to show the user a page for the next instance, in
 *  <code>display</code> mode. It is possible that this next instance cannot be
 *  found in persistent storage <code>(nextPageHandler.navigate(nextPageId) [!
 *  found(nextPageId)]</code>). In that case, we stay in <code>display</code>
 *  state in this page, and show the user a message.</p>
 * <p>This is not implemented in this generic handler.</p>
 * <h3>Edit</h3>
 * <p>In view mode <code>edit</code>, the data of the {@link
 *  be.peopleware.persistence_I.PersistentBean} is shown, in an editable way. This
 *  means that the user can change the current values of the properties of the
 *  persistent bean or fill in the value of properties that were not specified
 *  before.</p>
 * <h4>cancel</h4>
 * <p>From this state, we can try to return to the <code>display</code> state
 *  without applying the changes. When the bean is not found in persistent storage
 *  (<code>cancel(id) [!found(id)]</code>), the user is brought back to the
 *  previous page, and a message is shown. If the bean is found in persistent
 *  storage (<code>cancel(id) [found(id)]</code>), we return to the
 *  <code>display</code> state and the values that were filled in in the
 *  <code>edit</code> state are forgotten; this means that the old
 *  values of the bean are shown in the <code>display</code> state and that the
 *  persistent bean is unchanged.</p>
 * <p>This is implemented in action method {@link #cancelEdit()}. The user can
 *  request cancellation by clicking a button defined as:</p>
 * <pre>
 *   &lt;h:commandButton action=&quot;#{<var>myHandler</var>.cancelEdit}&quot;
 *                    value=&quot;#{<var>myHandler</var>.buttonLabels['cancel']}&quot;
 *                    rendered=&quot;#{<var>myHandler</var>.viewMode eq 'edit'}&quot;
 *                    immediate=&quot;true&quot;/&gt;
 * </pre>
 * <h4>update</h4>
 * <p>From the <code>edit</code> state, we can try to update the {@link
 *  be.peopleware.persistence_I.PersistentBean} with the values filled in by the
 *  user. When the bean is not found in persistent storage (<code>update(id, data)
 *  [! found(id)]</code>), the user is brought back to the
 *  previous page, and a message is shown. If the bean is found in persistent
 *  storage, updating might fail for semantic reasons (<code>update(id, data)
 *  [found(id) &amp;&amp; exception]</code>). In this case, we stay in the
 *  <code>edit</code> state, and show a message to the user about the exception.
 *  When updating succeeds (<code>update(id, data) [found(id) &amp;&amp;
 *  nominal]</code>), the handler goes to the <code>display</code> state and the
 *  persistent bean is updated in persistent storage.</p>
 * <p>This is implemented in action method {@link #update()}. The user can request
 *  storing the new information by clicking a button defined as:</p>
 * <pre>
 *   &lt;h:commandButton action=&quot;#{<var>myHandler</var>.update}&quot;
 *                    value=&quot;#{<var>myHandler</var>.buttonLabels['commit']}&quot;
 *                    rendered=&quot;#{<var>myHandler</var>.viewMode eq 'edit'}&quot; /&gt;
 * </pre>
 * <h3>Edit New</h3>
 * <p>In view mode <code>editNew</code>, the user is shown a page, with empty,
 *  editable fields for him to fill in. These fields represent a new bean of type
 *  {@link #getType()}. It is
 *  possible that some data is filled out when this page is shown, e.g., default
 *  data, or good guesses.</p>
 * <h4>cancel</h4>
 * <p>If the user changes his mind, he can request to cancel the
 *  <code>editNew</code> state (<code>cancel()</code>). This amounts to the user
 *  issuing a request to go back to the previous page (<code>goBack()</code>). If
 *  the previous instance cannot be found, this action propagates the user to
 *  instances visited earlier, recursively.</p>
 * <p>This is implemented in action method {@link #cancelEditNew()}. The user can
 *  request cancellation by clicking a button defined as:</p>
 * <pre>
 *   &lt;h:commandButton action=&quot;#{<var>myHandler</var>.cancelEditNew}&quot;
 *                    value=&quot;#{<var>myHandler</var>.buttonLabels['cancel']}&quot;
 *                    rendered=&quot;#{<var>myHandler</var>.viewMode eq 'editNew'}&quot;
 *                    immediate=&quot;true&quot;/&gt;
 * </pre>
 * <h4>create</h4>
 * <p>From view mode <code>editNew</code>, the user can submit data for the actual
 *  creation of a new bean in persistent storage. This can fail with semantic
 *  exceptions (<code>create(data) [exception]</code>). We stay in the
 *  <code>editNew</code> state, and show the user messages about the errors. If
 *  creation succeeds (<code>create(data) [nominal]</code>), the new instance is
 *  created in persistent storage, and the handler goes to the
 *  <code>display</code> state.</p>
 * <p>This is implemented in action method {@link #create()}. The user can request
 *  storing the new information by clicking a button defined as:</p>
 * <pre>
 *   &lt;h:commandButton action=&quot;#{<var>myHandler</var>.create}&quot;
 *                    value=&quot;#{<var>myHandler</var>.buttonLabels['commit']}&quot;
 *                    rendered=&quot;#{<var>myHandler</var>.viewMode eq 'editNew'}&quot; /&gt;
 * </pre>
 * <h3>Deleted</h3>
 * <p>In view mode <code>deleted</code>, the data of the deleted {@link
 *  be.peopleware.persistence_I.PersistentBean} is shown, in a non-editable way,
 *  with visual feedback about the fact that it was deleted (e.g., strikethrough).
 * <h4>goBack</h4>
 * <p>From this mode, the only action of the user can be to request to go back to
 *  the previous page (<code>goBack()</code>). If the previous instance cannot be
 *  found, this action propagates the user to instances visited earlier,
 *  recursively.</p>
 * <p>HOW IS THIS IMPLEMENTED? BUTTON?</p>
 * <h2>Remembering State</h2>
 * <p>With this setup, the only state that must be remembered in-between HTTP
 *  requests is the id of the {@link PersistentBean} we are working with, and the
 *  view mode of the handler. By storing this information in the actual HTML page,
 *  we essentially make the handler stateless.</p>
 * <p>This information needs to be filled out in the handler properties ({@link
 *  #getId()} and {@link #getViewMode()} before the action methods are executed. This
 *  can be achieved by storing these values in hidden text fields in the JSF page
 *  with the <code>immediate</code> attribute set to <code>true</code>:</p>
 * <pre>
 *   &lt;h:inputHidden value=&quot;#{<var>myHandler</var>.id}&quot; immediate=&quot;true&quot; /&gt;
 *   &lt;h:inputHidden value=&quot;#{<var>myHandler</var>.viewMode}&quot; immediate=&quot;true&quot; /&gt;</pre>
 * <p>
 *   Since the <code>immediate</code> attribute of these inputHidden tags is set
 *   to true, the corresponding setters {@link #setId(Long)} and
 *   {@link #setViewMode(String)} will be called early in the request / response
 *   cycle, namely during the Apply Request Values Phase. We give the
 *   {@link #setId(Long)} method side-effects that initialise the persistent bean
 *   and all other resources that are needed during the request / response cycle.
 *   In this way, the handler is initialised early in the request / response cycle.
 *   This is necessary, because, e.g., for some of the action methods
 *   ({@link #update()} and {@link #create()}), the bean must be available in
 *   {@link #getInstance()} before the Update Model Values phase, to receive
 *   values from the UIView.</br>
 *   If no instance with the requested id can be found in persistent storage,
 *   {@link #getInstance()} will be <code>null</code> afterwards to signal this.
 *   <br />
 *   The {@link #setId(Long)} method cannot however be used to create a new bean
 *   for entry into view mode <code>editNew</code>, and for the {@link #create()}
 *   action method. This is because the setter {@link #setId(Long)} will not be
 *   called when the request parameter for the id is <code>null</code>,
 *   since the id property is <code>null</code> already (see below).
 *   Therefore, creating a new bean is done as a side-effect in the
 *   {@link #setViewMode(String)} method, whenever the requested view mode
 *   is <code>editNew</code>.</p>
 * <p>To make sure that the hidden fields described above contain the correct values at the
 *  beginning of a request / response cycle, the corresponding handler must hold
 *  the correct values for the properties {@link #getId()} and {@link #getViewMode()}
 *  before the Render Response phase of the previous cycle is entered.</p>
 * <p>To guarantee that the setters {@link #setId(Long)} and
 *  {@link #setViewMode(String)} are called during the Apply Request Values
 *  Phase, the {@link #getId()} and {@link #getViewMode()} should be set to null
 *  after the Render Response Phase. This is because
 *  the {@link #setId(Long)} (resp. {@link #setViewMode(String)}) method is only
 *  executed when the old value of {@link #getId()} (resp. {@link #getViewMode()})
 *  and the <code>id</code> (resp. <code>viewMode</code>) that comes as a
 *  parameter with the HTTP request are different. To achieve this, the
 *  {@link #getId()} (resp. {@link #getViewMode()}) should be set to null at the
 *  end of each cycle.</p>
 * <p>JavaServer Faces does not offer the possibility to do anything after the
 *  response is rendered. So we need an extra mechanism that clears the handler.
 *  This is done by a special {@link ServletRequestListener} that magically knows which
 *  handler to reset.</p>
 * <h2>Not Remembering State</h2>
 * <p>After the response is rendered completely, the {@link #getId()} and {@link
 *  #getInstance()} (and possibly other resources) are no longer needed, and
 *  should be set to <code>null</code>, to avoid clogging memory in-between user
 *  requests. Setting the {@link #getId()} and {@link #getViewMode()} to null
 *  is also necessary for functional reasons, as described above. Releasing
 *  resources can be achieved by setting the handlers in request scope.</p>
 * <h2>Configuration</h2>
 * <p>This class requires the definition of 2 filters in
 *  <kbd>WEB-INF/web.xml</kbd>:</p>
 * <pre>
 * &lt;listener&gt;
 *   &lt;listener-class&gt;be.peopleware.servlet_I.hibernate.SessionFactoryController&lt;/listener-class&gt;
 *   &lt;listener-class&gt;be.peopleware.servlet_I.hibernate.SessionInView&lt;/listener-class&gt;
 * &lt;/listener&gt;
 * </pre>
 *
 *
 * @author    Jan Dockx
 * @author    Nele Smeets
 * @author    Peopleware n.v.
 *
 * @invar (getViewMode() != null)
 *            ? isViewMode(getViewMode())
 *            : true;
 * @invar (getInstance() != null)
 *            ? getType().isInstance(getInstance())
 *            : true;
 *
 * @idea (jand) gather viewmode in separate class
 * @mudo (jand) security
 */
public class PersistentBeanCrudHandler extends AbstractPersistentBeanHandler {

  /*<section name="Meta Information">*/
  //------------------------------------------------------------------
  /** {@value} */
  public static final String CVS_REVISION = "$Revision$";
  /** {@value} */
  public static final String CVS_DATE = "$Date$";
  /** {@value} */
  public static final String CVS_STATE = "$State$";
  /** {@value} */
  public static final String CVS_TAG = "$Name$";
  /*</section>*/


  private static final Log LOG = LogFactory.getLog(PersistentBeanCrudHandler.class);

  public PersistentBeanCrudHandler() {
    LOG.debug("constructor of PersistentBeanCrudHandler");
  }

//  public final void setDao(final AsyncCrudDao dao) {
//    super.setDao(dao);
//    if (getNavigationString() != null && getType() != null) {
//      ValueChangeEvent event = null;
//      setIdAndInitialisePersistentBean(event);
//      setViewModeAndInitialisePersistentBean(event);
//    }
//  }

//  public final void setNavigationString(final String navigationString) {
//    super.setNavigationString(navigationString);
//    if (getDao() != null && getType() != null) {
//      ValueChangeEvent event = null;
//      setIdAndInitialisePersistentBean(event);
//      setViewModeAndInitialisePersistentBean(event);
//    }
//  }

//  public final void setTypeAsString(final String type) {
//    super.setTypeAsString(type);
//    if (getDao() != null && getNavigationString() != null) {
//      ValueChangeEvent event = null;
//      setIdAndInitialisePersistentBean(event);
//      setViewModeAndInitialisePersistentBean(event);
//    }
//  }

  /*<property name="id">*/
  //------------------------------------------------------------------

  /**
   * The id of the {@link PersistentBean} that is handled by the requests.
   *
   * @basic
   * @init null;
   */
  public final Long getId() {
    return $id;
  }

  /**
   * Store the given id in {@link #getId()}.
   *
   * @param   id
   *          The id of the {@link PersistentBean} that will be handled in the
   *          requests.
   * @post    new.getId().equals(id);
   */
  public final void setId(final Long id) {
    // set the id
    $id = id;
    LOG.debug("id of " + this + " set to " + id);
  }

//  /**
//   * Retrieve the persistent bean of type {@link type} and id {@link id}
//   * from persistent storage, and store it in {@link #getInstance()}.
//   *
//   * If the necessary arguments and utilities are not set, exceptions are thrown.
//   *
//   * If no bean of type {@link type} with id {@link id} is found in
//   * persistent storage, {@link #getInstance()} is forced to <code>null</code>.
//   *
//   * @param     dao
//   *            The dao used to retrieve the {@link PersistentBean} from storage.
//   * @param     id
//   *            The id of the {@link PersistentBean} to retrieve.
//   * @param     type
//   *            The type of the {@link PersistentBean} to retrieve.
//   * @pre       dao != null;
//   * @post      (new.getInstance() != null)
//   *                ? id.equals(new.getInstance().getId())
//   *                : true;
//   * @post      (new.getInstance() != null)
//   *                ? type.isInstance(new.getInstance())
//   *                : true;
//   * @throws    IdException
//   *            id == null;
//   * @throws    IdException
//   *            type == null;
//   * @throws    TechnicalException tExc
//   *            ; something technical went wrong, but surely
//   *            ! (tExc instanceof IdNotFoundException)
//   */
//  private void retrieveWithId(final AsyncCrudDao dao, Long id, Class type)
//      throws IdException, TechnicalException {
//// (nsmeets) waarom worden die drie dingen als param meegegeven?
//    // mudo (jand) remove params
//    try {
//      assert dao != null;
//      //id or type are not known so passing the id to the exception is useless
//      if (id == null) {
//        LOG.error("id == null");
//        throw new IdException("ID_NULL", null, type);
//      }
//      if (type == null) {
//        LOG.error("type == null");
//        throw new IdException("TYPE_NULL", null, type);
//      }
//      LOG.debug("retrieving persistent bean with id "
//                  + id.toString() + " and type "
//                  + type.getName() + "...");
//      $instance = dao.retrievePersistentBean(id, type); // IdNotFoundException, TechnicalException
//      if (LOG.isDebugEnabled()) { // @mudo (nsmeets) consequent overal doen? Alleen bij dingen die veel vergen.
//        // if makes that there really is lazy loading if not in debug
//        LOG.debug("retrieved persistent bean is " + getInstance());
//      }
//      assert getInstance() != null;
//      assert getInstance().getId().equals(id);
//      assert type.isInstance(getInstance());
//    }
//    catch (IdNotFoundException e) {
//      // this will force $instance null
//      LOG.info("could not find instance of type "
//               + type.getName()
//               + " with id " + id, e);
//      $instance = null;
//    }
//    catch (TechnicalException e) {
//      LOG.error("exception during retrieveWithId", e);
//      throw e;
//    }
//  }

//  /**
//   * Store the given id and retrieve the corresponding {@link PersistentBean}
//   * from storage.
//   *
//   * Store the given id in {@link #getId()}.
//   * Load the {@link PersistentBean} with the given id, whose type is equal to
//   * {@link #getType()} from persistent storage and store this bean in
//   * {@link #getInstance()}.
//   * If no such bean is found in persistent storage, or when some
//   * technical exception occurs, {@link #getInstance()} is set
//   * to <code>null</code>.
//   *
//   * @param   id
//   *          The id of the {@link PersistentBean} that will be handled in the
//   *          requests.
//   * @post    new.getId().equals(id);
//   * @post    (new.getInstance() != null)
//   *            ? new.getInstance().getId().equals(id)
//   *            : true;
//   * @post    (new.getInstance() != null)
//   *            ? getType().isInstance(new.getInstance())
//   *            : true;
//   */
//  private final void setIdAndInitialisePersistentBean(final Long id) {
//    // set the id
//    setId(id);
//    // load the persistent bean with type getType() and the given id from
//    // persistent storage
//    try {
//      retrieveWithId(getDao(), id, getType()); // IdException, TechnicalException
//    }
//    catch(IdException exc) {
//      // This exception is thrown when id == null or getType() == null.
//      // 1. when id == null, then (normally) a new bean is created in
//      //    {@link #setViewMode}, so we leave {@link #getInstance()} unchanged
//      // 2. getType() cannot be null; the type of a handler should be declared
//      //    as a managed property in faces-config.xml
//    }
//    catch(TechnicalException exc) {
//      $instance = null;
//    }
//    // @idea (nsmeets) retrieve other resources
//  }

//  public void setIdAndInitialisePersistentBean(ValueChangeEvent event) {
//    Map requestParameterMap = RobustCurrent.externalContext().getRequestParameterMap();
//    // get id
//    String idString = (String)requestParameterMap.get("form:id");
//    Long id = null;
//    if (idString != null && !idString.equals("")) {
//      id = Long.valueOf(idString);
//    }
//    // set id
//    setIdAndInitialisePersistentBean(id);
//  }

  /**
   * The id of the {@link PersistentBean} that will be handled
   * by the requests.
   */
  private Long $id;

  /*</property>*/


  /*<property name="viewMode">*/
  //------------------------------------------------------------------

  /** {@value} */
  public final static String VIEWMODE_DISPLAY = "display";
  /** {@value} */
  public final static String VIEWMODE_EDIT = "edit";
  /** {@value} */
  public final static String VIEWMODE_EDITNEW = "editNew";
  /** {@value} */
  public final static String VIEWMODE_DELETED = "deleted";

  /**
   * { {@link #VIEWMODE_DISPLAY}, {@link #VIEWMODE_EDIT},
   *    {@link #VIEWMODE_EDITNEW}, {@link #VIEWMODE_DELETED} };
   */
  public final static String[] VIEWMODES
      = {VIEWMODE_DISPLAY, VIEWMODE_EDIT, VIEWMODE_EDITNEW, VIEWMODE_DELETED};

  /**
   * Does <code>viewMode</code> represent a valid view mode?
   *
   * @param   viewMode
   *          The viewMode to be checked.
   * @return  Arrays.asList(VIEWMODES).contains(s);
   */
  public static boolean isViewMode(String viewMode) {
    return Arrays.asList(VIEWMODES).contains(viewMode);
  }

  /**
   * The view mode of the handler.
   *
   * @basic
   * @init null;
   */
  public final String getViewMode() {
    return $viewMode;
  }

  /**
   * Set the view mode to the given string.
   *
   * @param   viewMode
   *          The view mode to set.
   * @post    (viewMode == null)
   *             ? new.getViewMode() == null
   *             : new.getViewMode().equals(viewMode);
   * @throws  IllegalArgumentException
   *          ! isViewMode(viewMode);
   */
  public final void setViewMode(String viewMode) throws IllegalArgumentException {
    if (! isViewMode(viewMode)) {
      throw new IllegalArgumentException("\"" + viewMode + "\" is not a valid view mode; " +
                                         "it must be one of " + VIEWMODES);
    }
    // set the view mode
    $viewMode = viewMode;
  }

//  /**
//   * Set the view mode to the given string and create a new
//   * {@link PersistentBean} when the given view mode is VIEWMODE_EDITNEW.
//   *
//   * Store the given string in {@link #getViewMode()}. When the given string
//   * is equal to VIEWMODE_EDITNEW, a new {@link PersistentBean} of type
//   * {@link #getType()} is created and stored in {@link #getInstance()}.
//   *
//   * @param   viewMode
//   *          The view mode to set.
//   * @post    (viewMode == null)
//   *             ? new.getViewMode() == null
//   *             : new.getViewMode().equals(viewMode);
//   * @post    ( (viewMode != null) && viewMode.equals(VIEWMODE_EDITNEW) )
//   *             ? new.getInstance() isfresh
//   *             : true;
//   * @throws  IllegalArgumentException
//   *          ! isViewMode(viewMode);
//   */
//  private final void setViewModeAndInitialisePersistentBean(String viewMode) throws IllegalArgumentException {
//    setViewMode(viewMode);
//    // When the view mode is equal to VIEWMODE_EDITNEW, then create
//    // a new instance of {@link getType()} and store it in {@link #getInstance()}
//    if ($viewMode.equals(VIEWMODE_EDITNEW)) {
//      createNewInstance();
//    }
//  }

//  public final void setViewModeAndInitialisePersistentBean(ValueChangeEvent event) {
//    Map requestParameterMap = RobustCurrent.externalContext().getRequestParameterMap();
//    // get view mode
//    String viewModeString = (String)requestParameterMap.get("form:viewMode");
//    // set view mode
//    if (viewModeString != null && !viewModeString.equals("")) {
//      setViewModeAndInitialisePersistentBean(viewModeString);
//    }
//  }

  /**
   * @invar ($viewMode != null)
   *            ? isViewMode($viewMode)
   *            : true;
   */
  private String $viewMode;

  /*</property>*/

  /**
   * Returns true when the handler is editable, i.e. when the view mode is
   * equal to VIEWMODE_EDIT or VIEWMODE_EDITNEW. Returns false otherwise.
   *
   * This method is introduces to avoid writing
   *  myHandler.viewmode eq 'edit' or myHandler.viewmode eq 'editNew'
   *  and
   *  myHandler.viewmode neq 'edit' and myHandler.viewmode neq 'editNew'
   * as value of the rendered attributes of in- and output fields in JSF pages,
   * which is cumbersome.
   * Now we can write
   *  myHandler.inViewModeEditOrEditNew
   * and
   *  not myHandler.inViewModeEditOrEditNew
   *
   * @return  getViewMode().equals(VIEWMODE_EDIT) ||
   *          getViewMode().equals(VIEWMODE_EDITNEW);
   * @throws  FatalFacesException
   *          getViewMode() == null;
   */
  public final boolean isInViewModeEditOrEditNew() throws FatalFacesException {
    if (getViewMode() == null) {
      RobustCurrent.fatalProblem("ViewMode is null", LOG);
      return false;
    }
    else {
      return
        getViewMode().equals(VIEWMODE_EDIT) ||
        getViewMode().equals(VIEWMODE_EDITNEW);
    }
  }


  /*<property name="instance">*/
  //------------------------------------------------------------------

  /**
   * The {@link PersistentBean} that is handled in the requests.
   *
   * @basic
   * @init null;
   */
  public final PersistentBean getInstance() {
    if ($instance == null) {
      if (getId() != null) {
        loadInstance();
      }
      else {
        createInstance();
      }
    }
    return $instance;
  }

  /**
   * @post new.getInstance() == instance;
   * @post (instance != null) ? new.getId().equals(instance.getId());
   * @throws IllegalArgumentException
   *         (instance != null) && ! getType().isAssignableFrom(instance.getClass());
   */
  public final void setInstance(PersistentBean instance) throws IllegalArgumentException {
    if ((instance != null) && (! getType().isAssignableFrom(instance.getClass()))) {
      throw new IllegalArgumentException("instance " + instance +
                                         " is not a subtype of " +
                                         getType());
    }
    $instance = instance;
    if (instance != null) {
      setId(instance.getId());
    }
    // else, we do NOT set the ID to null
  }

  /**
   * @pre getDao() != null;
   * @throws FatalFacesException
   *         {@link AsyncCrudDao#retrievePersistentBean(java.lang.Long, java.lang.Class)} / {@link TechnicalException}
   * @throws FatalFacesException
   *         MUDO (jand) other occurences must be replaced by goBack()
   */
  private void loadInstance() throws FatalFacesException {
    assert getDao() != null;
    try {
      if (getId() == null) {
        RobustCurrent.fatalProblem("id is null", LOG);
        // MUDO (jand) replace with goback?
      }
      if (getType() == null) {
        RobustCurrent.fatalProblem("type is null", LOG);
        // MUDO (jand) replace with goback?
      }
      LOG.debug("retrieving persistent bean with id "
                  + getId() + " and type " + getType() + "...");
      $instance = getDao().retrievePersistentBean(getId(), getType()); // IdNotFoundException, TechnicalException
      assert getInstance() != null;
      assert getInstance().getId().equals(getId());
      assert getType().isInstance(getInstance());
      if (LOG.isDebugEnabled()) {
        // if makes that there really is lazy loading if not in debug
        LOG.debug("retrieved persistent bean is " + getInstance());
      }
    }
    catch (IdNotFoundException infExc) {
      // this will force $instance null
      LOG.info("could not find instance of type " + getType() +
               " with id " + getId(), infExc);
      $instance = null;
      // MUDO goback() instead of exception
      RobustCurrent.fatalProblem("could not find persistent bean with id " +
                                 getId() + " of type " +
                                 getType(), infExc, LOG);
    }
    catch (TechnicalException tExc) {
      RobustCurrent.fatalProblem("could not retrieve persistent bean with id " +
                                 getId() + " of type " +
                                 getType(), tExc, LOG);
    }
  }


  /**
   * Create a new instance of type {@link #getType()} and store
   * it in {@link #getInstance()}.
   *
   * @post new.getInstance() isfresh
   * @post new.getInstance() == getType().newInstance();
   */
  private void createInstance() {
    try {
      $instance = (PersistentBean)getType().newInstance();
    }
    // all exceptions are programmatic errors here, in subclass, in config or in JSF
    catch (InstantiationException iExc) {
      assert false : "exception while creating new instance of type " + getType() + iExc;
    }
    catch (IllegalAccessException iaExc) {
      assert false : "exception while creating new instance of type " + getType() + iaExc;
    }
    catch (ExceptionInInitializerError eiiErr) {
      assert false : "exception while creating new instance of type " + getType() + eiiErr;
    }
    catch (SecurityException sExc) {
      assert false : "exception while creating new instance of type " + getType() + sExc;
    }
    catch (ClassCastException ccExc) {
      assert false : "exception while creating new instance of type " + getType() + ccExc;
    }
  }

  /**
   * Method to be called after Render Response phase, to clear
   * semantic data from the session.
   *
   * @post $instance == null;
   */
  private void releaseInstance() {
    $instance = null;
  }

  /**
   * The {@link PersistentBean} that is handled in the requests.
   */
  private PersistentBean $instance;

  /*</property>*/



  /**
   * This method should be called from within another handler when navigating
   * to this JSF page. A proper initialisation of this handler happens
   * to be able to show the {@link PersistentBean} whose id is given
   * in display mode.
   *
   * To initialise the handler properly, the following three steps are taken:
   * 1. Store the given id in {@link #getId()}.
   * 2. Load the {@link PersistentBean} with the given id, whose type is equal
   *    to {@link #getType()} from persistent storage and store this bean in
   *    {@link #getInstance()}. If no such bean is found in persistent storage,
   *    or when some technical exception occurs, this is signalled to the user
   *    by throwing an IdNotFoundException.
   * 3. We go to display mode.
   *
   * Navigate to {@link #getDetailViewId()}.
   *
   * This handler is made available to the JSP/JSF page in request scope,
   * as a variable with name {@link #getDetailHandlerName()}.
   *
   * @post    new.getId().equals(id);
   * @post    (new.getInstance() != null)
   *            ? new.getInstance().getId().equals(id)
   *            : true;
   * @post    (new.getInstance() != null)
   *            ? getType().isInstance(new.getInstance())
   *            : true;
   * @post    new.getViewMode().equals(VIEWMODE_DISPLAY);
   *
   * @mudo (jand) security
   */
  public final void navigateHere(ActionEvent aEv) throws FatalFacesException {
    assert getType() != null : "type cannot be null";
    LOG.debug("PersistentBeanCrudHandler.navigate called; initialising id and bean");
    if (getInstance() == null) {
      LOG.debug("no instance in " + this +
                "; cannot navigate; staying where we are");
//      String componentId = aEv.getComponent().getId();
      // MUDO (jand) add i18n message!!!!
    }
    setViewMode(VIEWMODE_DISPLAY);
    // put this handler in request scope, under an agreed name, create new view & navigate
    RobustCurrent.requestMap().put(getDetailHandlerName(), this);
    FacesContext context = RobustCurrent.facesContext();
    UIViewRoot viewRoot = RobustCurrent.viewHandler().createView(context, getDetailViewId());
    context.setViewRoot(viewRoot);
    context.renderResponse();
  }

  /**
   * <strong>= {@value}</strong>
   */
  public final static String DETAIL_HANDLER_NAME_SUFFIX = "H";

  /**
   * The name under which this handler is stored in request scope when
   * {@link #navigateHere(ActionEvent)} is called.
   * This name is the simple type name of {@link #getType()}, with the suffix
   * {@link #DETAIL_HANDLER_NAME_SUFFIX}.
   *
   * @idea (jand) make this parameterizable
   */
  public String getDetailHandlerName() {
    return getSimpleTypeName() + "H";
  }

  private final static String DOT = ".";

  /**
   * The simple type name of the type {@link #getType()}.
   */
  private String getSimpleTypeName() {
    String fqcn = getType().getName();
    String[] parts = fqcn.split(DOT);
    return parts[parts.length - 1];
  }

  public final static String DETAIL_VIEW_ID_PREFIX = "/jsf/";

  public final static String DETAIL_VIEW_ID_SUFFIX = ".jspx";

  /**
   * @pre getType() != null;
   * @return DETAIL_VIEW_ID_PREFIX + s/\./\//(getType().getName()) + DETAIL_VIEW_ID_SUFFIX;
   */
  public String getDetailViewId() {
    assert getType() != null : "type cannot be null";
    String typeName = getType().getName();
    typeName = typeName.replace('.', '/');
    return DETAIL_VIEW_ID_PREFIX + typeName + DETAIL_VIEW_ID_SUFFIX;
  }

  /**
   * This is an action method that should be called by a button in the JSF
   * page to go to edit mode.
   *
   * A more detailed description of this action method can be found in the
   * class description.
   *
   * @post (getInstance() == null)
   *         ? 'return to previous page' &&
   *           result.equals(NO_INSTANCE)
   *         : true;
   * @post (getInstance() != null && !getViewMode().equals(VIEWMODE_DISPLAY))
   *         ? getViewMode().equals(VIEWMODE_DISPLAY) &&
   *           result.equals(null)
   *         : true;
   * @post (getInstance() != null && getViewMode().equals(VIEWMODE_DISPLAY))
   *         ? getViewMode().equals(VIEWMODE_EDIT) &&
   *           result.equals(null)
   *         : true;
   * @mudo (jand) security
   */
  public final String edit() {
    LOG.debug("PersistentBeanCrudHandler.edit called; showing bean for edit");
    LOG.debug("persistentBean: " + getInstance());
    try {
      checkConditions(VIEWMODE_DISPLAY); // ConditionException
      setViewMode(VIEWMODE_EDIT);
      return getNavigationString();
    }
    catch(ConditionException exc) {
      return exc.getNavigationString();
    }
  }

  /**
   * This is an action method that should be called by a button in the JSF
   * page to update a persistent bean in persistent storage.
   *
   * A more detailed description of this action method can be found in the
   * class description.
   *
   * @post    (getInstance() == null)
   *            ? 'return to previous page' &&
   *              result.equals(NO_INSTANCE)
   *            : true;
   * @post    (getInstance() != null && !getViewMode().equals(VIEWMODE_EDIT))
   *            ? getViewMode().equals(VIEWMODE_DISPLAY) &&
   *              result.equals(null)
   *            : true;
   * @post    (getInstance() != null && getViewMode().equals(VIEWMODE_EDIT)
   *               && 'updateValues generated messages')
   *            ? getViewMode().equals(VIEWMODE_EDIT) &&
   *              result.equals(null)
   *            : true;
   * @post    (getInstance() != null && getViewMode().equals(VIEWMODE_EDIT)
   *               && 'updateValues succeeded without messages'
   *               && 'update in storage generates no semantic exceptions')
   *            ? 'bean is updated in persistent storage' &&
   *              getViewMode().equals(VIEWMODE_DISPLAY) &&
   *              result.equals(null)
   *            : true;
   * @post    (getInstance() != null && getViewMode().equals(VIEWMODE_EDIT)
   *               && 'updateValues succeeded without messages'
   *               && 'update in storage generates semantic exceptions')
   *            ? getViewMode().equals(VIEWMODE_EDIT) &&
   *              result.equals(null)
   *            : true;
   * @throws  FatalFacesException
   *          When a TechnicalException is thrown by:
   *          {@link AsyncCrudDao#startTransaction()}
   *          {@link AsyncCrudDao#updatePersistentBean(be.peopleware.persistence_I.PersistentBean)}
   *          {@link AsyncCrudDao#commitTransaction(be.peopleware.persistence_I.PersistentBean)}
   *          {@link AsyncCrudDao#cancelTransaction()}
   */
  public String update() throws FatalFacesException {
    LOG.debug("PersistentBeanCrudHandler.update called; the bean is already partially updated");
    LOG.debug("persistentBean: " + getInstance());
    try {
      AsyncCrudDao dao = getDao();
      try {
        checkConditions(VIEWMODE_EDIT); // ConditionException
        updateValues();
        LOG.debug("The bean is now fully updated");
        LOG.debug("persistentBean: " + getInstance());
        if (RobustCurrent.hasMessages()) {
          // updateValues can create FacesMessages that signal semantic errors
          return null;
        }
        else {
          dao.startTransaction(); // TechnicalException
          dao.updatePersistentBean(getInstance()); // TechnicalException, CompoundPropertyException
          dao.commitTransaction(getInstance()); // TechnicalException, CompoundPropertyException
          setViewMode(VIEWMODE_DISPLAY);
          return null;
        }
      }
      catch(CompoundPropertyException cpExc) {
        LOG.debug("update action failed; cancelling ...", cpExc);
        dao.cancelTransaction(); // TechnicalException
        LOG.debug("update action cancelled; using exception as faces message");
        RobustCurrent.showCompoundPropertyException(cpExc);
        setViewMode(VIEWMODE_EDIT);
        return null;
      }
      catch(ConditionException exc) {
        return exc.getNavigationString();
      }
    }
    catch(TechnicalException exc) {
      RobustCurrent.fatalProblem("Could not update " + getInstance(), exc, LOG);
      return null;
    }
  }

  /**
   * <p>
   * This method can be used to update properties of {@link #getInstance()}
   * that are not updated during the Update Model Values.
   * </p>
   * <p>
   * Suppose that a JSF page contains the following tag:
   * </p>
   * <pre>
   *   &lt;h:inputText value=&quot;#{<var>myHandler</var>.instance.name}&quot; /&gt;
   * </pre>
   * <p>
   * During the Update Model Values phase, the setName(String) method of the
   * bean stored in {@link #getInstance()} will be called, thereby updating
   * the value of the name property. Similarly, other properties are updated.
   * </p>
   * <p>
   * But there can also be properties of a {@link PersistentBean} that are not
   * updated 'automatically' during the Update Model Values phase. An example
   * of this is a property <code>date</code> of type {@link java.util.Date},
   * that is represented in the JSF page by three inputText tags representing
   * year, month and day.
   * <p>
   * <pre>
   *   &lt;h:inputText value=&quot;#{<var>myHandler</var>.year}&quot; /&gt;
   *   &lt;h:inputText value=&quot;#{<var>myHandler</var>.month}&quot; /&gt;
   *   &lt;h:inputText value=&quot;#{<var>myHandler</var>.day}&quot; /&gt;
   * </pre>
   * <p>
   * Because the values of these tags do not correspond directly
   * to a property in the {@link PersistentBean}, the tags are backed by
   * three properties ($year, $month, $day) in the handler with corresponding
   * get and set methods. During the Update Model Values phase, the three
   * properties are updated in the handler. The bean itself can then be
   * updated during the Invoke Application phase, using the
   * {@link #updateValues()} method. The implementation of this method could
   * then be:
   * </p>
   * <pre>
   *   Date date
   *   = (new GregorianCalendar(getYear(), getMonth(), getDay())).getTime();
   *   ((SomeType) getInstance()).setDate(date);
   * </pre>
   * <p>
   *   The default implementation of this method does nothing.
   * </p>
   */
  protected void updateValues() {
    // NOP
  }

  /**
   * This method should be called from within another handler to pass
   * a newly created persistent bean of type {@link #getType()} and show this
   * bean in editNew mode. To do this, the handler should be properly
   * initialised.
   *
   * A more detailed description of this action method can be found in the
   * class description.
   *
   * To initialise the handler properly, the following two steps are taken:
   * 1. The given {@link PersistentBean} is stored in {@link #getInstance()}.
   *    If the given bean is not effective, has an effective id (i.e. is not
   *    newly created), or is not of type {@link #getType()}, this is signalled
   *    to the user by throwing an InvalidBeanException.
   * 2. We go to editNew mode.
   *
   * @param   instance
   *          The {@link PersistentBean} that should be displayed.
   * @post    new.getInstance() == instance;
   * @post    new.getViewMode().equals(VIEWMODE_EDITNEW);
   * @throws  InvalidBeanException
   *          instance == null ||
   *          instance.getId() != null ||
   *          !getType().isInstance(instance);
   */
  public final void editNew(PersistentBean instance) throws InvalidBeanException {
    LOG.debug("PersistentBeanCrudHandler.editNew called; a new instance is stored in the handler");
    if (instance == null || instance.getId() != null || !getType().isInstance(instance)) {
      throw new InvalidBeanException(instance, getType());
    }
    $instance = instance;
    assert getInstance() != null;
    assert getInstance().getId() == null;
    assert getType().isInstance(instance);
    setViewMode(VIEWMODE_EDITNEW);
    LOG.debug("Stored new persistent bean successfully");
  }

  public class InvalidBeanException extends Exception {

    public InvalidBeanException(PersistentBean persistentBean, Class type) {
      $persistentBean = persistentBean;
      $type = type;
    }

    public PersistentBean getPersistentBean() {
      return $persistentBean;
    }

    public Class getType() {
      return $type;
    }

    private PersistentBean $persistentBean;
    private Class $type;
  }

  /**
   * This is an action method that should be called by a button in the JSF
   * page to add a newly created persistent bean to persistent storage.
   *
   * A more detailed description of this action method can be found in the
   * class description.
   *
   * @post    (getInstance() == null)
   *            ? 'return to previous page' &&
   *              result.equals(NO_INSTANCE)
   *            : true;
   * @post    (getInstance() != null && !getViewMode().equals(VIEWMODE_EDITNEW))
   *            ? 'return to previous page' &&
   *              result.equals(INCORRECT_VIEWMODE)
   *            : true;
   * @post    (getInstance() != null && getViewMode().equals(VIEWMODE_EDITNEW)
   *               && 'updateValues generated messages')
   *            ? getViewMode().equals(VIEWMODE_EDITNEW) &&
   *              result.equals(null)
   *            : true;
   * @post    (getInstance() != null && getViewMode().equals(VIEWMODE_EDITNEW)
   *               && 'updateValues succeeded without messages'
   *               && 'update in storage generates no semantic exceptions')
   *            ? 'bean is created in persistent storage' &&
   *              new.getId() == new.getInstance().getId()
   *              getViewMode().equals(VIEWMODE_DISPLAY) &&
   *              result.equals(null)
   *            : true;
   * @post    (getInstance() != null && getViewMode().equals(VIEWMODE_EDITNEW)
   *               && 'updateValues succeeded without messages'
   *               && 'update in storage generates semantic exceptions')
   *            ? getViewMode().equals(VIEWMODE_EDITNEW) &&
   *              result.equals(null)
   *            : true;
   * @throws  FatalFacesException
   *          When a TechnicalException is thrown by:
   *          {@link AsyncCrudDao#startTransaction()}
   *          {@link AsyncCrudDao#createPersistentBean(be.peopleware.persistence_I.PersistentBean)}
   *          {@link AsyncCrudDao#commitTransaction(be.peopleware.persistence_I.PersistentBean)}
   *          {@link AsyncCrudDao#cancelTransaction()}
   */
  public final String create() throws FatalFacesException {
    LOG.debug("PersistentBeanCrudHandler.create called; a new bean is created and is "+
        "already partially updated");
    LOG.debug("persistentBean: " + getInstance());
    try {
      AsyncCrudDao dao = getDao();
      try {
        if (getInstance() == null) {
          return goBack(NO_INSTANCE);
        }
        if (!getViewMode().equals(VIEWMODE_EDITNEW)) {
          return goBack(INCORRECT_VIEWMODE);
        }
        updateValues();
        LOG.debug("The bean is now fully updated");
        LOG.debug("persistentBean: " + getInstance());
        if (RobustCurrent.hasMessages()) {
          // updateValues can create FacesMessages that signal semantic errors
          return null;
        }
        else {
          dao.startTransaction(); // TechnicalException
          dao.createPersistentBean(getInstance()); // TechnicalException, CompoundPropertyException
          dao.commitTransaction(getInstance()); // TechnicalException, CompoundPropertyException
          assert getInstance().getId() != null;
          setId(getInstance().getId());
          setViewMode(VIEWMODE_DISPLAY);
          return null;
        }
      }
      catch(CompoundPropertyException cpExc) {
        LOG.debug("create action failed; cancelling ...", cpExc);
        dao.cancelTransaction(); // TechnicalException
        LOG.debug("create action cancelled; using exception as faces message");
        RobustCurrent.showCompoundPropertyException(cpExc);
        setViewMode(VIEWMODE_EDITNEW);
        return null;
      }
    }
    catch(TechnicalException exc) {
      RobustCurrent.fatalProblem("Could not create " + getInstance(), exc, LOG);
      return null;
    }
  }

  public static final String NO_INSTANCE = "NO_INSTANCE";
  public static final String INCORRECT_VIEWMODE = "INCORRECT_VIEWMODE";
  public static final String CANCEL_EDITNEW = "CANCEL_EDITNEW";

  /**
   * This is an action method that should be called by a button in the JSF
   * page to delete a persistent bean from persistent storage.
   *
   * A more detailed description of this action method can be found in the
   * class description.
   *
   * @post    (getInstance() == null)
   *            ? 'return to previous page' &&
   *              result.equals(NO_INSTANCE)
   *            : true;
   * @post    (getInstance() != null && !getViewMode().equals(VIEWMODE_DISPLAY))
   *            ? getViewMode().equals(VIEWMODE_DISPLAY) &&
   *              result.equals(null)
   *            : true;
   * @post    (getInstance() != null && getViewMode().equals(VIEWMODE_DISPLAY)
   *               && 'delete in storage generates no semantic exceptions')
   *            ? 'bean is deleted from persistent storage' &&
   *              getViewMode().equals(VIEWMODE_DELETED) &&
   *              result.equals(null)
   *            : true;
   * @post    (getInstance() != null && getViewMode().equals(VIEWMODE_DISPLAY)
   *               && 'delete in storage generates semantic exceptions')
   *            ? getViewMode().equals(VIEWMODE_DISPLAY) &&
   *              result.equals(null)
   *            : true;
   * @throws  FatalFacesException
   *          When a TechnicalException is thrown by:
   *          {@link AsyncCrudDao#startTransaction()}
   *          {@link AsyncCrudDao#deletePersistentBean(be.peopleware.persistence_I.PersistentBean)}
   *          {@link AsyncCrudDao#commitTransaction(be.peopleware.persistence_I.PersistentBean)}
   *          {@link AsyncCrudDao#cancelTransaction()}
   */
  public final String delete() throws FatalFacesException {
    LOG.debug("PersistentBeanCrudHandler.delete() called");
    LOG.debug("persistentBean: " + getInstance());
    AsyncCrudDao dao = getDao();
    try {
      try {
        checkConditions(VIEWMODE_DISPLAY); // ConditionException
        dao.startTransaction(); // TechnicalException
        dao.deletePersistentBean(getInstance()); // TechnicalException
        dao.commitTransaction(getInstance());// TechnicalException, CompoundPropertyException
        assert getInstance().getId() == null;
        setViewMode(VIEWMODE_DELETED);
        return getNavigationString();
      }
      catch(ConditionException exc) {
        return exc.getNavigationString();
      }
      catch (CompoundPropertyException cpExc) {
        LOG.debug("delete action failed; cancelling ...", cpExc);
        dao.cancelTransaction(); // TechnicalException
        LOG.debug("delete action cancelled; using exception as faces message");
        RobustCurrent.showCompoundPropertyException(cpExc);
        setViewMode(VIEWMODE_DISPLAY);
        return null;
      }
    }
    catch(TechnicalException exc) {
      RobustCurrent.fatalProblem("Could not delete " + getInstance(), exc, LOG);
      return null;
    }
  }

  /**
   * Helper method used in action methods to check whether the persistent bean
   * is effective and whether the current view mode corresponds to the
   * view mode in which the action method should be called.
   * If one of these conditions is not met, appropriate actions are taken.
   *
   * When {@link #getInstance()} is <code>null</code>, we return to a previous
   * page.
   * When the {@link #getInstance()} is effective, but the current view mode
   * does not correspond to the given expected view mode, we go to display mode.
   * In both cases, a ConditionException is thrown that contains the
   * navigation string.
   *
   * @param   expectedViewMode
   *          The view mode that should be checked.
   * @pre     isViewMode(expectedViewMode);
   * @post    true
   * @throws  ConditionException exc
   *          getInstance() == null
   *            && exc.getOutcome().equals(NO_INSTANCE);
   *          As a side effect, we return to a previous page.
   * @throws  ConditionException exc
   *          getInstance() != null && !getViewMode().equals(expectedViewMode)
   *            && exc.getOutcome().equals(display());
   *          As a side effect, we go to display mode.
   */
  private void checkConditions(String expectedViewMode) throws ConditionException {
    assert isViewMode(expectedViewMode);
    String result = null;
    if (getInstance() == null) {
      result = NO_INSTANCE;
      goBack(result);
      throw new ConditionException(result);
    }
    else if (!expectedViewMode.equals(getViewMode())) {
      setViewMode(VIEWMODE_DISPLAY);
      throw new ConditionException(null);
    }
  }

  /**
   * A class of exceptions that is used when checking whether an action method
   * is called under the correct conditions.
   *
   * A navigation string describes where to go when a certain condition is not
   * met.
   *
   * @author nsmeets
   */
  private class ConditionException extends Exception {
    public ConditionException(String navigationString) {
      $navigationString = navigationString;
    }

    public String getNavigationString() {
      return $navigationString;
    }

    private String $navigationString;
  }

  /**
   * Method to be called after Render Response phase, to clear
   * semantic data from the session.
   *
   * @post getInstance() == null;
   */
  void release() {
    releaseInstance();
    // mudo (jand) more code or remove?
  }

  /**
   * This method returns to the page that was visited before this page.
   * It is possible that this previous page cannot be displayed anymore
   * (e.g. because the corresponding {@link PersistentBean} does not exist in
   * persistent storage anymore), so this process is recursive, until the first
   * page after login is reached.
   * The method returns the navigation string needed to navigate to the
   * previous page
   *
   * @param   message
   *          A message signalling why we are going back to a previous page.
   * @return  @mudo
   */
  public String goBack(String message) {
//  MUDO
    return message;
  }

  /**
   *
   */
  public String goBack() {
    LOG.debug("goBack method called");
    return null;
  }

  /**
   * This is an action method that should be called by a button in the JSF
   * page to cancel the update of an existing persistent bean
   * (i.e. a persistent bean that was loaded from persistent storage,
   * meaning that {@link #getInstance()} <code>!=null</code> and
   * {@link #getInstance()}.{@link #getId()} <code>!=null</code>).
   *
   * A more detailed description of this action method can be found in the
   * class description.
   *
   * @post    (getInstance() == null)
   *            ? 'return to previous page' &&
   *              result.equals(NO_INSTANCE)
   *            : true;
   * @post    (getInstance() != null && !getViewMode().equals(VIEWMODE_EDIT))
   *            ? getViewMode().equals(VIEWMODE_DISPLAY) &&
   *              result.equals(null)
   *            : true;
   * @post    (getInstance() != null && getViewMode().equals(VIEWMODE_EDIT))
   *            ? 'reset the UI components' &&
   *              getViewMode().equals(VIEWMODE_DISPLAY) &&
   *              result.equals(null)
   *            : true;
   */
  public final String cancelEdit() {
    LOG.debug("PersistentBeanCrudHandler.cancelEdit called; showing bean");
    LOG.debug("persistentBean: " + getInstance());
    try {
      checkConditions(VIEWMODE_EDIT); // ConditionException
      RobustCurrent.resetUIInputComponents();
      setViewMode(VIEWMODE_DISPLAY);
      return null;
    }
    catch(ConditionException exc) {
      return exc.getNavigationString();
    }
  }

  /**
   * This is an action method that should be called by a button in the JSF
   * page to cancel the creation of a new persistent bean
   * (i.e. a persistent bean that was created in memory but not saved in
   * persistent storage yet).
   *
   * A more detailed description of this action method can be found in the
   * class description.
   *
   * @post    'return to previous page'
   * @post    (getInstance() == null)
   *            ? result.equals(NO_INSTANCE)
   *            : true;
   * @post    (getInstance() != null && !getViewMode().equals(VIEWMODE_EDITNEW))
   *            ? result.equals(INCORRECT_VIEWMODE)
   *            : true;
   * @post    (getInstance() != null && getViewMode().equals(VIEWMODE_EDITNEW))
   *            ? result.equals(CANCEL_EDITNEW)
   *            : true;
   */
  public final String cancelEditNew() {
    LOG.debug("PersistentBeanCrudHandler.cancelEditNew called; returning to previous page");
    LOG.debug("persistentBean: " + getInstance());
    if (getInstance() == null) {
      return goBack(NO_INSTANCE);
    }
    if (!getViewMode().equals(VIEWMODE_EDITNEW)) {
      return goBack(INCORRECT_VIEWMODE);
    }
    return goBack(CANCEL_EDITNEW);
  }

}
