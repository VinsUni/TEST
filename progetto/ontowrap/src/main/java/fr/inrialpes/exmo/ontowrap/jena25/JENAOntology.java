/*
 * $Id: JENAOntology.java 1757 2012-07-20 12:16:12Z jdavid $
 *
 * Copyright (C) INRIA, 2003-2008, 2010, 2012
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.ontowrap.jena25;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import fr.inrialpes.exmo.ontowrap.Annotation;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;

/**
 *class JENAOntology
 */
public class JENAOntology extends BasicOntology<OntModel> implements HeavyLoadedOntology<OntModel>{

    // JE: this is not very Java 1.5...
    // This is because of the version of Jena we use apparently

	/**
	 *
	 *
	 * @param u an URI
	 * @return
	 * @throws OntowrapException
	 */
    public Object getEntity(URI u) throws OntowrapException {
	return onto.getOntResource(u.toString());
    }

	/**
	 *
	 * @param o
	 * @param annots
	 * @param lang
	 * @throws OntowrapException
	 */
    @Deprecated
    protected void getEntityAnnotations( Object o, Set<String> annots, String lang) throws OntowrapException {
	getEntityAnnotations(o,annots,lang, new HashSet<Object>());
    }

	/**
	 *
	 * @param o
	 * @param annots
	 * @param lang
	 * @param entitiesTraversed
	 * @throws OntowrapException
	 */
    protected void getEntityAnnotations( Object o, Set<String> annots, String lang, Set<Object> entitiesTraversed) throws OntowrapException {
	StmtIterator stmtIt = onto.listStatements((Resource)o,null,(RDFNode)null);
	while (stmtIt.hasNext()) {
	    Statement st = stmtIt.next();
	    
	    if ( st.getPredicate().canAs(AnnotationProperty.class)) {
		RDFNode obj= st.getObject();
		if (obj.isLiteral()) {
		    Literal l =obj.as(Literal.class);
		    if (lang==null || lang.equals(l.getLanguage())) {
			annots.add(l.getLexicalForm());
		    }
		}
		else if (obj.isResource() && !entitiesTraversed.contains(st.getSubject())) {
		    entitiesTraversed.add(st.getSubject());
		    getEntityAnnotations(obj, annots, lang, entitiesTraversed);
		}
	    }
	}
    }

	/**
	 *
	 * @param o
	 * @param annots
	 * @throws OntowrapException
	 */
    @Deprecated
    protected void getEntityAnnotations( Object o, Set<Annotation> annots) throws OntowrapException {
	getEntityAnnotations(o,annots,new HashSet<Object>());
    }

	/**
	 *
	 * @param o
	 * @param annots
	 * @param entitiesTraversed
	 * @throws OntowrapException
	 */
    protected void getEntityAnnotations( Object o, Set<Annotation> annots, Set<Object> entitiesTraversed) throws OntowrapException {
	StmtIterator stmtIt = onto.listStatements((Resource)o,null,(RDFNode)null);
	while (stmtIt.hasNext()) {
	    Statement st = stmtIt.next();
	    
	    if ( st.getPredicate().canAs(AnnotationProperty.class)) {
		RDFNode obj= st.getObject();
		if (obj.isLiteral()) {
		    Literal l =obj.as(Literal.class);
		    annots.add(new Annotation(l.getLexicalForm(),l.getLanguage()));
		}
		else if (obj.isResource() && !entitiesTraversed.contains(st.getSubject())) {
		    entitiesTraversed.add(st.getSubject());
		    getEntityAnnotations(obj, annots,entitiesTraversed);
		 }
	    }
	}
    }

	/**
	 *
	 * @param o the entity
	 * @return
	 * @throws OntowrapException
	 */
    public Set<String> getEntityAnnotations(Object o) throws OntowrapException {
	Set<String> annots = new HashSet<String>();
	getEntityAnnotations(o,annots,null, new HashSet<Object>());
	return annots;
    }

	/**
	 *
	 * @param o
	 * @return
	 * @throws OntowrapException
	 */
    public Set<Annotation> getEntityAnnotationsL(Object o) throws OntowrapException {
	Set<Annotation> annots = new HashSet<Annotation>();
	getEntityAnnotations(o,annots,new HashSet<Object>());
	return annots;
    }

	/**
	 *
	 * @param o the entity
	 * @param lang the code of the language ("en", "fr", "es", etc.)
	 * @return
	 * @throws OntowrapException
	 */
    public Set<String> getEntityAnnotations( Object o, String lang ) throws OntowrapException {
	Set<String> annots = new HashSet<String>();
	getEntityAnnotations(o,annots,lang,new HashSet<Object>());
	return annots;
    }

	/**
	 *
	 * @param o the entity
	 * @param lang the code of the language ("en", "fr", "es", etc.)
	 * @return
	 * @throws OntowrapException
	 */
    public Set<String> getEntityComments(Object o, String lang) throws OntowrapException {
	Set<String> comments = new HashSet<String>();
	Iterator<RDFNode> it = ((OntResource)o).listComments(lang);
	while (it.hasNext()) {
	    comments.add( ((LiteralImpl)it.next()).getLexicalForm() );
	}
	return comments;
    }

	/**
	 *
	 * @param o the entity
	 * @return
	 * @throws OntowrapException
	 */
    public Set<String> getEntityComments(Object o) throws OntowrapException {
	return getEntityComments(o,null);
    }

	/**
	 *
	 * @param o the entity
	 * @return
	 * @throws OntowrapException
	 */
    public String getEntityName( Object o ) throws OntowrapException {
	try {
	    // Should try to get labels first... (done in the OWLAPI way)
	    return getFragmentAsLabel( new URI( ((OntResource) o).getURI() ) );
	} catch ( Exception oex ) {
	    return null;
	}
    }

	/**
	 *
	 * @param o the entity
	 * @param lang
	 * @return
	 * @throws OntowrapException
	 */
    public String getEntityName( Object o, String lang ) throws OntowrapException {
	// Should first get the label in the language
	return getEntityName( o );
    }

	/**
	 *
	 * @param o the entity
	 * @param lang the code of the language ("en", "fr", "es", etc.)
	 * @return
	 * @throws OntowrapException
	 */
    public Set<String> getEntityNames(Object o, String lang) throws OntowrapException {
	Set<String> labels = new HashSet<String>();
	OntResource or = (OntResource) o;
	Iterator<?> i = or.listLabels(lang);
	while (i.hasNext()) {
	    String label = ((LiteralImpl) i.next()).getLexicalForm();
	    labels.add(label);
	}
	return labels;
    }

	/**
	 *
	 * @param o the entity
	 * @return
	 * @throws OntowrapException
	 */
    public Set<String> getEntityNames(Object o) throws OntowrapException {
	return getEntityNames(o,null);
    }

	/**
	 *
	 * @param o the entity
	 * @return
	 * @throws OntowrapException
	 */
    public URI getEntityURI(Object o) throws OntowrapException {
	try {
	    OntResource or = (OntResource) o;
	    return new URI(or.getURI());
	} catch (Exception e) {
	    throw new OntowrapException(o.toString()+" do not have uri", e );
	}
    }

    /*
     * This strange structure, as well as the corresponding JENAEntityIt
     * is there only because Jena may return unammed entities that have to
     * be filtered out from the sets.
     *
     */
    /*protected <R extends OntResource> Set<R> getEntitySet(final ExtendedIterator<R> i) {
	return new AbstractSet<R>() {
	    private int size=-1;
	    public Iterator<R> iterator() {
		return new JENAEntityIt( getURI(), i );
	    }
	    public int size() {
		if (size==-1) {
		    for (R r : this)
			size++;
		    size++;
		}
		return size;
	    }
	};
    }*/

	/**
	 *
	 * @return
	 */
    public Set<OntClass> getClasses() {
	return new EntityFilter<OntClass>(onto.listNamedClasses().toSet(),this);
    }

	/**
	 *
	 * @return
	 */
    public Set<DatatypeProperty> getDataProperties() {
	return new EntityFilter<DatatypeProperty>(onto.listDatatypeProperties().toSet(),this);
	//return getEntitySet(onto.listDatatypeProperties());
    }

    protected final static Map1<OntProperty,OntResource> mapProperty = new Map1<OntProperty,OntResource> () { public OntResource map1 ( OntProperty o ) { return o; } };
    protected final static Map1<OntClass,OntResource> mapClass = new Map1<OntClass,OntResource> () { public OntResource map1 ( OntClass o ) { return o; } };
    protected final static Map1<Individual,OntResource> mapInd = new Map1<Individual,OntResource> () { public OntResource map1 ( Individual o ) { return o; } };
    protected final static Map1<OntClass,Object> mapClassToObj = new Map1<OntClass,Object> () { public Object map1 ( OntClass o ) { return o; } };

	/**
	 *
	 * @return
	 */
    public Set<OntResource> getEntities() {
	return new EntityFilter<OntResource>((onto.listAllOntProperties().mapWith( mapProperty )
		    .andThen(onto.listNamedClasses().mapWith( mapClass ))
		    .andThen(onto.listIndividuals().mapWith( mapInd )).toSet()),
		    this);
    }

	/**
	 *
	 * @return
	 */
    public Set<Individual> getIndividuals() {
	return new EntityFilter<Individual>(onto.listIndividuals().toSet(),this);
    }

	/**
	 *
	 * @return
	 */
    public Set<ObjectProperty> getObjectProperties() {
	return new EntityFilter<ObjectProperty>(onto.listObjectProperties().toSet(),this);
    }

	/**
	 *
	 * @return
	 */
    public Set<OntProperty> getProperties() {
	return new EntityFilter<OntProperty>(onto.listAllOntProperties().toSet(),this);
	/*return getEntitySet( onto.listAllOntProperties().filterDrop( new Filter () {
		public boolean accept( Object o ) { return (o instanceof AnnotationProperty); }
	    }) );*/
	//return getEntitySet(onto.listObjectProperties().andThen(onto.listDatatypeProperties()));
    }

	/**
	 *
	 * @param o
	 * @return
	 */
    public boolean isClass(Object o) {
	return o instanceof OntClass;
    }

	/**
	 *
	 * @param o
	 * @return
	 */
	 public boolean isDataProperty(Object o) {
	 return o instanceof DatatypeProperty;
	 }

	/**
	 *
	 * @param o
	 * @return
	 */
	 public boolean isEntity(Object o) {
	 return isClass(o)||isProperty(o)||isIndividual(o);
	 }

	/**
	 *
	 * @param o
	 * @return
	 */
	 public boolean isIndividual(Object o) {
	 return o instanceof Individual;
	 }

	/**
	 *
	 * @param o
	 * @return
	 */
	 public boolean isObjectProperty(Object o) {
	 return o instanceof ObjectProperty;
	 }

	/**
	 *
	 * @param o
	 * @return
	 */
	 public boolean isProperty(Object o) {
	 return o instanceof OntProperty;
	 }

	/**
	 *
	 * @return
	 */
	 public int nbEntities() {
	 return this.getEntities().size();
	 }
	 /**
	 *
	 * @return
	 */
	public int nbClasses() {
	return this.getClasses().size();
    }

    public int nbDataProperties() {
	return this.getDataProperties().size();
    }

    public int nbIndividuals() {
	return this.getIndividuals().size();
    }

    public int nbObjectProperties() {
	return this.getObjectProperties().size();
    }

    public int nbProperties() {
	return this.getProperties().size();
    }

    public void unload() {

    }

	/**
	 *
	 * @param Direct
	 * @param Asserted
	 * @param Named
	 * @return
	 */
    public boolean getCapabilities(int Direct, int Asserted, int Named) {
	return true;
    }

	/**
	 *
	 * @param i
	 * @param local
	 * @param asserted
	 * @param named
	 * @return
	 */
    public Set<OntClass> getClasses(Object i, int local, int asserted, int named) {
	return ((Individual) i).listOntClasses(asserted==OntologyFactory.DIRECT).toSet();
    }

	/**
	 *
	 * @param c
	 * @param local
	 * @param asserted
	 * @param named
	 * @return
	 */
    public Set<OntProperty> getDataProperties(Object c, int local, int asserted, int named) {
	return new EntityFilter<OntProperty>( ((OntClass) c).listDeclaredProperties(asserted==OntologyFactory.DIRECT).toSet(),this) {
	    protected boolean isFiltered(OntProperty obj) {
		return super.isFiltered(obj) && !obj.isDatatypeProperty();
	    }
	    
	};
    }

	/**
	 *
	 * @param p
	 * @param asserted
	 * @return
	 */
    public Set<? extends OntResource> getDomain(Object p, int asserted) {
	return ((OntProperty) p).listDomain().toSet();
    }

	/**
	 *
	 * @param c
	 * @param local
	 * @param asserted
	 * @param named
	 * @return
	 */
    public Set<?> getInstances(Object c, int local, int asserted, int named) {
	if (c instanceof OntClass) {
	    return ((OntClass) c).listInstances(asserted==OntologyFactory.DIRECT).toSet();
	}
	return null;
	
    }

	/**
	 *
	 * @param c
	 * @param local
	 * @param asserted
	 * @param named
	 * @return
	 */
    public Set<OntProperty> getObjectProperties(Object c, int local, int asserted, int named) {
	return new EntityFilter<OntProperty>( ((OntClass) c).listDeclaredProperties(asserted==OntologyFactory.DIRECT).toSet(),this) {
	    protected boolean isFiltered( OntProperty obj ) {
		return super.isFiltered(obj) && !obj.isObjectProperty();
	    }  
	};
    }

	/**
	 *
	 * @param c
	 * @param local
	 * @param asserted
	 * @param named
	 * @return
	 */
    public Set<OntProperty> getProperties(Object c, int local, int asserted, int named) {
	return new EntityFilter<OntProperty>( ((OntClass) c).listDeclaredProperties(asserted==OntologyFactory.DIRECT).toSet(),this);
    }

	/**
	 *
	 * @param p
	 * @param asserted
	 * @return
	 */
    public Set<? extends OntResource> getRange(Object p, int asserted) {
	return ((OntProperty) p).listRange().toSet();
    }

	/**
	 *
	 * @param c
	 * @param local
	 * @param asserted
	 * @param named
	 * @return
	 */
    public  Set<? extends OntClass> getSubClasses(Object c, int local, int asserted, int named) {
	return ((OntClass) c).listSubClasses(asserted==OntologyFactory.DIRECT).toSet();
    }

	/**
	 *
	 * @param p
	 * @param local
	 * @param asserted
	 * @param named
	 * @return
	 */
    public Set<? extends OntProperty> getSubProperties(Object p, int local, int asserted, int named) {
	return ((OntProperty) p).listSubProperties(asserted==OntologyFactory.DIRECT).toSet();
    }

	/**
	 *
	 * @param c
	 * @param local
	 * @param asserted
	 * @param named
	 * @return
	 */
    public Set<OntClass> getSuperClasses(Object c, int local, int asserted, int named) {
	return ((OntClass) c).listSuperClasses(asserted==OntologyFactory.DIRECT).toSet();
    }

	/**
	 *
	 * @param p
	 * @param local
	 * @param asserted
	 * @param named
	 * @return
	 */
    public Set<? extends OntProperty> getSuperProperties(Object p, int local, int asserted, int named) {
	return ((OntProperty) p).listSuperProperties(asserted==OntologyFactory.DIRECT).toSet();
    }
}

/*class JENAEntityIt<R extends OntResource> implements Iterator<R> {

    private ExtendedIterator<R> it;
    private R current;
    private URI ontURI;
    
    public JENAEntityIt( URI ontURI, ExtendedIterator<R> entityIt ) {
	this.ontURI = ontURI;
	this.it = entityIt;
    }

    private void setNext() {
	while (current==null) {
	    current = it.next();
	    if (current.getURI()==null) {// || !current.getURI().startsWith(ontURI.toString())) {
		current=null;
	    }
	}
    }
    public boolean hasNext() {
	try {
	    setNext();
	    return current!=null;
	}
	catch (NoSuchElementException e) {
	    return false;
	}
    }

    public R next() {
	setNext();
	R returnR = current;
	current=null;
	return returnR;
    }

    public void remove() {
	throw new UnsupportedOperationException();
    }
}*/
