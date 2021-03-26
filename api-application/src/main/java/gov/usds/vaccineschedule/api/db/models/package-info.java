@GenericGenerators({
        @GenericGenerator(name = "UUID4", strategy = "org.hibernate.id.UUIDGenerator"),
        @GenericGenerator(
                name = "UUID1",
                strategy = "org.hibernate.id.UUIDGenerator",
                parameters = {
                        @Parameter(
                                name = "uuid_gen_strategy_class",
                                value = "org.hibernate.id.uuid.CustomVersionOneStrategy")
                })
})
@TypeDefs({
        @TypeDef(name = "list-array", typeClass = ListArrayType.class),
})
package gov.usds.vaccineschedule.api.db.models;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.GenericGenerators;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
