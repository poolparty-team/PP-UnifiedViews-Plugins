package eu.unifiedviews.plugins.swc.poolparty.util;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.util.Collection;
import java.util.Set;

public class TestRdfDataUnit implements RDFDataUnit {

    private Repository repo;

    public TestRdfDataUnit(Collection<Statement> statements) throws RepositoryException {
        repo = new SailRepository(new MemoryStore());
        repo.initialize();
        repo.getConnection().add(statements);
    }

    @Override
    public RepositoryConnection getConnection() throws DataUnitException {
        try {
            return repo.getConnection();
        }
        catch (RepositoryException e) {
            throw new DataUnitException(e);
        }
    }

    @Override
    public Set<URI> getMetadataGraphnames() throws DataUnitException {
        return null;
    }

    @Override
    public Iteration getIteration() throws DataUnitException {
        return null;
    }

}
