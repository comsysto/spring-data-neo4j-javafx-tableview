package com.comsysto.neo4j.domain;

import com.comsysto.util.Neo4jTableBuilderColumnField;
import com.comsysto.util.Neo4jTableBuilderColumnSetMethod;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/** @author Elisabeth Engel */
@NodeEntity
public class Neo4jCustomer implements Neo4jNode {

    static private Integer nextIdValue = 1;

    @GraphId
    private Long graphId;

    @Indexed(indexName = "customerIntIdIndex")
    private Integer intId;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "customerName")
    private String customerName;

    private String firstname;

    private String lastname;

    @Neo4jTableBuilderColumnField(columnName = "Signup Date", columnOrder = 3)
    private Date signupDate;

    @Neo4jTableBuilderColumnField(columnName = "#Knows", columnType = Neo4jTableBuilderColumnField.FieldType.readOnly,  columnOrder = 10)
    private Integer amountKnows;

    @Fetch
    @RelatedTo(type = "KNOWS", direction = Direction.BOTH)
    private Set<Neo4jCustomer> friends = new HashSet<Neo4jCustomer>();

    public Neo4jCustomer() {
        this("[new]", "[new]");
    }

    public Neo4jCustomer(String firstname, String lastname) {
        this(firstname, lastname, new Date());
    }

    public Neo4jCustomer(String firstname, String lastname, Date signupDate) {

        this.intId = nextIdValue++;

        this.customerName = firstname+" "+lastname;
        this.firstname = firstname;
        this.lastname = lastname;
        this.signupDate = signupDate;
        this.amountKnows = 0;
    }

    @Override
    public Integer getId() {
        return intId;
    }

    @Override
    public boolean isSaved() {
        return graphId != null;
    }

    public void setId(Integer id) {
        this.intId = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getFirstname() {
        return firstname;
    }

    @Neo4jTableBuilderColumnSetMethod(columnName = "Firstname", columnOrder = 1)
    public void setFirstname(String firstname) {
        this.firstname = firstname;
        this.customerName = firstname+" "+lastname;
    }

    public String getLastname() {
        return lastname;
    }

    @Neo4jTableBuilderColumnSetMethod(columnName = "Lastname", columnOrder = 2)
    public void setLastname(String lastname) {
        this.lastname = lastname;
        this.customerName = firstname+" "+lastname;
    }

    public Date getSignupDate() {
        return signupDate;
    }

    public void setSignupDate(Date signupDate) {
         this.signupDate = signupDate;
    }

    public void addFriend(Neo4jCustomer newFriend) {
        friends.add(newFriend);
        if (!newFriend.areFriends(this)) newFriend.addFriend(this);
        amountKnows++;
    }

    public boolean areFriends(Neo4jCustomer otherNeo4jCustomer) {
        return friends.contains(otherNeo4jCustomer);
    }

    public Integer getAmountKnows() {
        //return friends.size();
        return amountKnows;
    }


    @Override
    public String toString() {
        return "Neo4jCustomer{" +
                "graphId=" + graphId +
                ", intId=" + intId +
                ", customerName=" + customerName +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", signupDate='" + signupDate + '\'' +
                ", #friends=" + friends.size() +
                ", #knows=" + getAmountKnows() +
                '}';
    }
}
