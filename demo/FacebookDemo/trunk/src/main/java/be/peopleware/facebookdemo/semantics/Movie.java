package be.peopleware.facebookdemo.semantics;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.ppwcode.vernacular.persistence_III.dao.jpa.CreateAllowedBy;
import org.ppwcode.vernacular.persistence_III.dao.jpa.UpdateAllowedBy;
import org.ppwcode.vernacular.persistence_III.jpa.AbstractIntegerIdIntegerVersionedPersistentBean;

@NamedQueries({
  @NamedQuery(name  = "findMoviesByFacebookUser",
              query = "SELECT m FROM Movie AS m WHERE m.$facebookUserId LIKE :fbuid ")
})
@Entity
@Table(name="movie")
@CreateAllowedBy("anonymous")
@UpdateAllowedBy("anonymous")
public class Movie extends AbstractIntegerIdIntegerVersionedPersistentBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4545746267436384281L;

	/*
	 * <property name="facebook user">
	 * -------------------------------------------------------------------------
	 */
	public void setFacebookUserId(long id) {
		$facebookUserId = id;
	}

	public long getFacebookUserId() {
		return $facebookUserId;
	}

	@Column(name = "fbuid")
	private long $facebookUserId = 0;

	/* </property */

	/*
	 * <property name="id">
	 * -------------------------------------------------------------------------
	 */
	public String getId() {
		return $id;
	}

	public void setId(String id) {
		$id = id;
	}

	@Column(name = "id", nullable = false)
	private String $id = null;

	/* </property> */
	
	/*
	 * <property name="name">
	 * -------------------------------------------------------------------------
	 */
	
	public String getName() {
		return $name;
	}
	
	public void setName(String name) {
		$name = name;
	}
	
	@Column(name = "name", nullable = false)
	private String $name = null;
	
	/* </property> */
	
	/*
	 * <property name="media type">
	 * -------------------------------------------------------------------------
	 */
	public MediaType getMediaType() {
		return $mediaType;
	}

	public void setMediaType(MediaType newType) {
		$mediaType = newType;
	}

	@Column(name = "mediatype", nullable = false)
	private MediaType $mediaType = MediaType.OTHER;

	/* </property> */

	/*
	 * <property name="rating">
	 * -------------------------------------------------------------------------
	 */
	public int getRating() {
		return $rating;
	}

	public void setRating(int rating) {
		if (rating < 0) {
			$rating = 0;
		} else if (rating > 5) {
			$rating = 5;
		} else {
			$rating = rating;
		}
	}

	private int $rating = 0;
	/* </property> */
	
	/*
	 * <property name="comment">
	 * -------------------------------------------------------------------------
	 */
	public String getComment() {
		return $comment;
	}
	
	public void setComment(String comment) {
		$comment = comment;
	}
	
	private String $comment;
	/* </property> */

}
