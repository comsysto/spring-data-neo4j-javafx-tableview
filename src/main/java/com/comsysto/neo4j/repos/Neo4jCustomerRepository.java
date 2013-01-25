package com.comsysto.neo4j.repos;

import com.comsysto.neo4j.domain.Neo4jCustomer;
import org.springframework.data.neo4j.repository.GraphRepository;

/** @author Elisabeth Engel */
public interface Neo4jCustomerRepository extends GraphRepository<Neo4jCustomer> {

    Iterable<Neo4jCustomer> findByCustomerNameLike(String customerName);

}
