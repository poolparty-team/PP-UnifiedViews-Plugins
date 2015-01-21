package eu.unifiedviews.plugins.swc.poolparty.util;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.util.Set;

/**
* Created by christian on 21.01.15.
*/
public class TestWritableRdfUnit implements WritableRDFDataUnit {

    private Repository repo;

    public TestWritableRdfUnit() throws RepositoryException {
        repo = new SailRepository(new MemoryStore());
        repo.initialize();
    }

    @Override
    public URI getBaseDataGraphURI() throws DataUnitException {
        return null;
    }

    @Override
    public void addExistingDataGraph(String symbolicName, URI existingDataGraphURI) throws DataUnitException {

    }

    @Override
    public URI addNewDataGraph(String symbolicName) throws DataUnitException {
        return null;
    }

    @Override
    public RepositoryConnection getConnection() throws DataUnitException {
        try {
            return repo.getConnection();
        }
        catch (RepositoryException e) {
            throw new DataUnitException("Error establishing repository connection");
        }
    }

    @Override
    public Set<URI> getMetadataGraphnames() throws DataUnitException {
        return null;
    }

    @Override
    public RDFDataUnit.Iteration getIteration() throws DataUnitException {
        return null;
    }

    @Override
    public void addEntry(String symbolicName) throws DataUnitException {

    }

    @Override
    public URI getMetadataWriteGraphname() throws DataUnitException {
        return null;
    }
}
