CREATE TABLE KIM_PRINCIPALS_T (
        ID NUMBER(8) NOT NULL,
        NAME VARCHAR2(500) NOT NULL,
		OBJ_ID VARCHAR2(36) DEFAULT SYS_GUID() NOT NULL, 
        VER_NBR NUMBER(8) DEFAULT 1 NOT NULL,
        CONSTRAINT KIM_PRINCIPALS_PK PRIMARY KEY (ID)
)
/