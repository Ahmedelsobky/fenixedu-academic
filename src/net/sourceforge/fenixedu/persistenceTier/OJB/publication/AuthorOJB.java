/*
 * Created on 22/Nov/2003
 *
 */
package net.sourceforge.fenixedu.persistenceTier.OJB.publication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.fenixedu.commons.CollectionUtils;
import net.sourceforge.fenixedu.domain.publication.Author;
import net.sourceforge.fenixedu.domain.publication.IAuthor;
import net.sourceforge.fenixedu.domain.publication.IPublication;
import net.sourceforge.fenixedu.domain.publication.IPublicationAuthor;
import net.sourceforge.fenixedu.domain.publication.PublicationAuthor;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;
import net.sourceforge.fenixedu.persistenceTier.IPersistentPublicationAuthor;
import net.sourceforge.fenixedu.persistenceTier.OJB.PersistentObjectOJB;
import net.sourceforge.fenixedu.persistenceTier.publication.IPersistentAuthor;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.Transformer;
import org.apache.ojb.broker.query.Criteria;

/**
 * @author TJBF & PFON
 *  
 */
public class AuthorOJB extends PersistentObjectOJB implements IPersistentAuthor {

    /**
     *  
     */
    public AuthorOJB() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ServidorPersistente.teacher.IPersistentOldPublication#readAllByTeacherAndOldPublicationType(Dominio.ITeacher,
     *      Util.OldPublicationType)
     */
    public Author readAuthorByKeyPerson(Integer keyPerson) throws ExcepcaoPersistencia {
        Criteria criteria = new Criteria();
        criteria.addEqualTo("keyPerson", keyPerson);
        Author author = (Author) queryObject(Author.class, criteria);
        return author;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ServidorPersistente.publication.IPersistentAuthor#readAuthorsBySubName(java.lang.String)
     */
    public List readAuthorsBySubName(String stringSubName) throws ExcepcaoPersistencia {

        List authors = new ArrayList();

        Criteria criteria = new Criteria();
        criteria.addLike("author", stringSubName);

        authors = queryList(Author.class, criteria);

        return authors;
    }
    
    /*
     * This method deletes the author and all entries in Publication_Auhtors that
     * belong to this author
     */
    public void delete (Object obj) throws ExcepcaoPersistencia {
        super.delete(obj);
        IPersistentPublicationAuthor ppa = new PublicationAuthorOJB();
        
        IAuthor author = (IAuthor) obj;
        List publicationAuthors = new ArrayList(author.getAuthorPublications());
        Collections.sort(publicationAuthors, new BeanComparator("order"));
        List publicationList = (List)CollectionUtils.collect(publicationAuthors,
                new Transformer() {
                    public Object transform(Object object) {
                        PublicationAuthor publicationAuthor = (PublicationAuthor) object;
                        return publicationAuthor.getPublication();
        }});
        
        Iterator it = publicationList.iterator();
        while (it.hasNext()){
            IPublication publication = (IPublication)it.next();
            IPublicationAuthor pa = new PublicationAuthor();
            pa.setKeyAuthor(author.getIdInternal());
            pa.setKeyPublication(publication.getIdInternal());
            ppa.deleteByOID(pa.getClass(),pa.getIdInternal());
        }
    }

}