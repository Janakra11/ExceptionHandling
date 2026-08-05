🗄️ Core Architecture & Mapping Strategy
Q1: Why did you choose @ManyToOne(fetch = FetchType.LAZY) over the default EAGER fetching strategy for 
your entity relations?

Answer: 
    
    By default, JPA configures @ManyToOne and @OneToOne associations to use FetchType.EAGER. 
    If left unchanged, loading a list of 100 employees would cause Hibernate to fire 100 
    separate sub-queries to fetch the department metadata for each row—a classic performance bottleneck
    known as the N+1 Query Problem.
    To prevent this, I explicitly switched all relational fields (department, roles, projects) 
    to FetchType.LAZY. 
    This ensures that Hibernate only pulls base employee primitives initially. 
    Related child collections are only loaded when explicitly requested inside an active transaction boundary.

Q2: In your bidirectional @OneToMany mapping inside Department.java, why did you implement custom helper 
methods like addEmployee() and removeEmployee()?

Answer: 
    
    In a bidirectional JPA relationship, only the side that holds the @JoinColumn mapping—the 
    @ManyToOne side in Employee—acts as the owning side responsible for updating the foreign key column 
    in the database. The @OneToMany side in Department is marked with mappedBy and is completely ignored 
    by Hibernate's database flush engine.If you only update the collection inside Department 
    (e.g., department.getEmployees().add(employee)), 
    the foreign key column in the MySQL database will remain NULL. 
    I wrote explicit helper methods to enforce bidirectional state synchronization automatically:
    java   public void addEmployee(Employee employee) {
                this.employees.add(employee);
                employee.setDepartment(this); // Updates the owning side to guarantee foreign key persistence
            }

⚡ Query Optimization & Performance Tuning

Q3: How exactly did you resolve the N+1 Query Problem for your bulk read endpoints?

Answer: 
    
    I bypassed Hibernate's automatic lazy loading proxy mechanisms by writing an explicit 
    JPQL LEFT JOIN FETCH query inside the EmployeeRepository:java
        @Query("SELECT DISTINCT e FROM Employee e 
                LEFT JOIN FETCH e.department 
                LEFT JOIN FETCH e.roles LEFT JOIN FETCH e.projects")
        List<Employee> findAllWithDetails();
    Use code with caution.Instead of firing an initial query followed by separate sub-queries 
    for every record's relations, the FETCH keyword instructs Hibernate's SQL translation engine 
    to generate a single LEFT OUTER JOIN statement. 
    This pulls the employee data and all associated metadata records down from MySQL in a single 
    database round-trip.

Q4: How did you implement database-level pagination alongside relationship join fetches without 
tanking application memory?

Answer: 
    
    Passing a basic Pageable payload straight into a query containing multiple 
    JOIN FETCH statements forces Hibernate to fetch the entire un-paged dataset into the application's 
    heap memory to perform pagination in memory. 
    This generates a critical warning 
    (HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!) 
    and can easily crash your container on large production tables.To resolve this, 
    I explicitly provided a separated countQuery inside the @Query annotation:
    java
    @Query(value = "SELECT DISTINCT e FROM Employee e 
    LEFT JOIN FETCH e.department 
    LEFT JOIN FETCH e.roles 
    LEFT JOIN FETCH e.projects", countQuery = "SELECT COUNT(e) FROM Employee e")
    Page<Employee> findAllWithDetailsPaginated(Pageable pageable);
   
    Use code with caution.Providing a clean count query allows Hibernate to safely issue a 
    lightweight count statement first, and then apply native SQL LIMIT and OFFSET clauses directly 
    to the core join query on the MySQL instance.

🔄 Transaction Management & Context States

Q5: What is the benefit of adding @Transactional(readOnly = true) to your read-only service methods 
like getAllEmployees()?

Answer: 
    
    Marking a transaction as readOnly = true provides a major optimization boost at both the Hibernate
    and database level:Hibernate Optimization: It instructs Hibernate to disable its internal 
    dirty-checking mechanism. Since Hibernate knows the data cannot mutate, it skips generating 
    snapshot representations of the loaded entities, significantly reducing memory consumption 
    and CPU cycles during flushes.MySQL 
    Database Optimization: It allows the underlying MySQL JDBC driver to route connections directly 
    to read-replicas (if configured) and optimizes internal lock mechanics, preventing shared read 
    locks from escalating into exclusive write blocks.

Q6: Explain the difference between an entity being in a Managed state versus a Detached state, and 
why you used entityManager.detach() during debugging.

Answer:
        
    Managed State: The entity is actively linked to the current Hibernate Persistence Context 
    (First-Level Cache). Any setters called on the object will automatically be synchronized with 
    the database when the transaction commits, without needing to call .save().

    Detached State: The entity still has a valid primary key identifier matching a row in the database, 
    but it is no longer tracked by the Hibernate session. Changes made to it are ignored.
    
    I utilized entityManager.detach(employee) to force Hibernate to drop its dirty-checking tracking 
    metrics for an employee instance.This forced the subsequent findByIdWithDetails(id) lookup to run a true, fresh SQL inner-join 
    query against MySQL, rather than pulling the dirty proxied entity from the local first-level 
    session cache.

🛡️ Data Integrity, Lifecycle, & Validation

Q7: What Isolation Level did you use for this project, and how does it prevent concurrency anomalies 
like Dirty Reads or Non-Repeatable Reads?

Answer: 

    This application utilizes the default isolation level of MySQL, which is REPEATABLE_READ.
    Dirty Reads Prevention: It completely blocks dirty reads because it enforces a read-committed 
    barrier—transactions can never view uncommitted row mutations from concurrent threads.
    Non-Repeatable Reads Prevention: If a transaction reads an employee record at timestamp A, 
    and another transaction updates that same employee row at timestamp B, re-reading the data 
    within the first transaction will still yield the exact snapshot state from timestamp A. 
    MySQL achieves this using MVCC (Multi-Version Concurrency Control), ensuring data consistency 
    over long business logic scopes.

Q8: How does your application protect the database from receiving malformed input data using 
Hibernate Validator annotations?

Answer: 

    I implemented standard Jakarta Bean Validation constraints straight on the entity model layer 
    (e.g., @NotBlank, @Email, @PastOrPresent). When a request enters the controller decorated 
    with @Valid, Spring immediately invokes the Hibernate Validation engine.
    If any validation fails (e.g., an invalid email format), 
    Hibernate prevents the entity from touching the persistence layer 
    and throws a MethodArgumentNotValidException. 
    
    This keeps database records completely clean and isolates business validation processing 
    from leaking down into MySQL constraint failures.

💡 JPA Interview Cheat-Sheet: Top Red Flags to Avoid
When answering these questions in front of a Senior Architect, 
remember to mention these production keywords:
            
    Never say you use EAGER fetching to fix exceptions. 
    Always explain how you use JOIN FETCH or custom DTO projections instead.
    Emphasize that you use saveAndFlush() when caching layers are present, 
    as regular save() defers synchronization and can cause your Redis cache to store stale entity data.
    Explicitly note that you use Lombok's @Getter and @Setter 
    but never use @Data or @ToString on JPA entities, 
    because auto-generated hashCode() and toString() methods will print lazy collections 
    and cause fatal stack overflow recursion crashes.
    Would you like to prepare a simulated technical review on any other subsystem of this project, 
    such as your custom Global Exception Handling architecture?
