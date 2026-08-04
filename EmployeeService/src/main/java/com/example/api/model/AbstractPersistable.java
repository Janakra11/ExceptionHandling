package com.example.api.model;


import jakarta.persistence.*;
import org.hibernate.annotations.DialectOverride;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
public abstract class AbstractPersistable<P extends Serializable> implements Persistable<P> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
    @SequenceGenerator(name="hibernate_sequence", sequenceName = "HIBERNATE_SEQUENCE", allocationSize = 1)
    private P id;

    @Override
    public @Nullable P getId() {
        return id;
    }

    protected void setId(P id) {
        this.id = id;
    }

    @Transient
    @Override
    public boolean isNew(){ return null == getId();}

    @Override
    public String toString(){
        return "AbstractPersistable{"+
                "id="+id+"}";
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof AbstractPersistable)) return false;
        AbstractPersistable<?> that = (AbstractPersistable<?>) o;
        return Objects.equals(id, this.id);
    }

    public int hashcode(){ return Objects.hash(id);}

}
