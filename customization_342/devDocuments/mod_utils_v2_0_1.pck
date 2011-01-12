CREATE OR REPLACE PACKAGE mod_utils IS

PROCEDURE debug
(p_client_id                 IN NUMBER,
 p_org_id                    IN NUMBER,
 p_debug                     IN NUMBER,
 p_batch_id                  IN NUMBER,
 p_level                     IN NUMBER,
 p_callfromtype              IN VARCHAR2,
 p_process                   IN VARCHAR2,
 p_mesg						           IN VARCHAR2);
 
FUNCTION getattr(
 p_client_id                 NUMBER,
 p_org_id                    NUMBER,
 p_debug                     NUMBER,
 p_batch_id                  IN NUMBER,
 p_attrsetinst_id            NUMBER,
 p_source_type               VARCHAR2,
 p_source_id                 NUMBER,
 p_attribute                 VARCHAR2
)
RETURN VARCHAR;

PROCEDURE setattr(
 p_debug                     NUMBER,
 p_batch_id                  IN NUMBER,
 p_client_id                 NUMBER,
 p_org_id                    NUMBER,
 p_attrsetinst_id            NUMBER,
 p_source_type               VARCHAR2,
 p_source_id                 NUMBER,
 p_attribute                 VARCHAR2,
 p_new_value                 VARCHAR2 DEFAULT NULL);

PROCEDURE newsetinst(
 p_debug                     NUMBER,
 p_batch_id                  IN NUMBER,
 p_attrsetinst_id            IN OUT NUMBER,
 p_client_id                 IN     NUMBER,
 p_org_id                    IN     NUMBER,
 p_source_type               IN     VARCHAR2,
 p_source_id                 IN     NUMBER,
 p_attrset_name              IN     VARCHAR2);

PROCEDURE setinstdescr(
 p_client_id                 NUMBER,
 p_org_id                    NUMBER,
 p_debug                     NUMBER,
 p_batch_id                  IN NUMBER,
 p_attrsetinst_id            IN NUMBER);

END mod_utils;
/
CREATE OR REPLACE PACKAGE BODY mod_utils IS

--================================================================================================
-- History
--   1.0.0 01-JUN-08 Created       
--   1.1.0 23-AUG-08 Altered logging logic
--   1.2.0 19-JAN-09 Alter logic of debug
--   2.0.1 28-AUG-10 Make debug autonomous
--================================================================================================

g_trapped                    EXCEPTION;
g_debug                      NUMBER;
g_err                        NUMBER;
g_errm                       VARCHAR2(1000);

--================================================================================================
-- Write debug data
-- Debug Levels
-- 0 - Required system Messages.
-- 1 - Fatile error - Not expected
-- 2 - Fatile error - Trapped
-- 3 - Unfatile error - Trapped.
-- 4 - Useful info
-- 5 - Debug data
--================================================================================================

PROCEDURE debug
(p_client_id                 IN NUMBER,
 p_org_id                    IN NUMBER,
 p_debug                     IN NUMBER,
 p_batch_id                  IN NUMBER,
 p_level                     IN NUMBER,
 p_callfromtype              IN VARCHAR2,
 p_process                   IN VARCHAR2,
 p_mesg						           IN VARCHAR2) IS

PRAGMA AUTONOMOUS_TRANSACTION;

l_log_id NUMBER;
 
BEGIN

   --IF p_level IN (1,2,3) THEN
   --  ROLLBACK;
   --END IF;
   
   IF p_level <= p_debug THEN
	    DBMS_OUTPUT.PUT_LINE(p_level||':'||p_process||'->'||p_mesg);
      IF p_callfromtype <> 'F' THEN
         SELECT mod_log_s.nextval INTO l_log_id FROM dual;
         INSERT INTO mod_log(ad_client_id, ad_org_id,
                isactive, created, createdby, updated, updatedby,
                log_date,mesg_level,process,mesg,batch_id, log_id)
         VALUES(p_client_id, p_org_id, 
                'Y',SYSDATE,0,SYSDATE,0,
                SYSDATE, p_level, p_process, p_mesg, p_batch_id, l_log_id);
      END IF;
   END IF;

   COMMIT;
   
   --IF p_level IN (1,2,3) THEN
   --   COMMIT;
   --END IF;
   
END debug;

--================================================================================================
-- Sleetc a product attribute value
--================================================================================================

FUNCTION getattr(
 p_client_id                 NUMBER,
 p_org_id                    NUMBER,
 p_debug                     NUMBER,
 p_batch_id                  NUMBER,
 p_attrsetinst_id            NUMBER,
 p_source_type               VARCHAR2,
 p_source_id                 NUMBER,
 p_attribute                 VARCHAR2
)
RETURN VARCHAR
IS
v_result                     VARCHAR2(200);
BEGIN

g_debug  := p_debug;

IF p_attrsetinst_id IS NULL THEN
   mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,3,'F','mod_utils.getattr','NULLINSTID:'||p_source_type||':'||p_source_id);
   v_result := NULL;
ELSE
   BEGIN

     SELECT NVL(v.description,i.value)
     INTO   v_result
     FROM   m_attributeinstance i,
            m_attribute a,
            m_attributevalue v
     WHERE  i.m_attributesetinstance_id     = p_attrsetinst_id
     AND    i.m_attribute_id                = a.m_attribute_id
     AND    UPPER(a.name)                   = UPPER(p_attribute)
     AND    i.m_attributevalue_id           = v.m_attributevalue_id(+);

   EXCEPTION

   WHEN NO_DATA_FOUND THEN
     mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,2,'F','mod_utils.getattr','NODATA:'||p_attrsetinst_id||':'||p_source_type||':'||p_source_id||':'||p_attribute);
     v_result := NULL;

   WHEN TOO_MANY_ROWS THEN
     mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,2,'F','mod_utils.getattr','TOOMANY:'||p_attrsetinst_id||':'||p_source_type||':'||p_source_id||':'||':'||p_attribute);
     v_result := NULL;
     RAISE g_trapped;
     
   END;
END IF;

RETURN v_result;

EXCEPTION 
WHEN OTHERS THEN
     g_err := SQLCODE;
     g_errm := SQLERRM;
     mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,1,'F','mod_utils.getattr','UNKNOWN:'||g_err||':'||g_errm||':'||p_attrsetinst_id||':'||p_source_type||':'||p_source_id||':'||p_attribute);
     RAISE;

END getattr;
     
--================================================================================================
-- Set a product attribute value
--================================================================================================

PROCEDURE setattr(
 p_debug                     NUMBER,
 p_batch_id                  IN NUMBER,
 p_client_id                 NUMBER,
 p_org_id                    NUMBER,
 p_attrsetinst_id            NUMBER,
 p_source_type               VARCHAR2,
 p_source_id                 NUMBER,
 p_attribute                 VARCHAR2,
 p_new_value                 VARCHAR2 DEFAULT NULL
)
IS

v_attribute_id               NUMBER;
v_attributevalue_id          NUMBER;
v_valuetype                  VARCHAR2(1);
v_new_value                  VARCHAR2(200);

BEGIN

g_debug  := p_debug;

   -- Look to see if atrribute already exists against instance
   BEGIN

     SELECT i.m_attribute_id,
            v.m_attributevalue_id
     INTO   v_attribute_id,
            v_attributevalue_id
     FROM   m_attributeinstance i,
            m_attribute a,
            m_attributevalue v
     WHERE  i.m_attributesetinstance_id     = p_attrsetinst_id
     AND    i.m_attribute_id                = a.m_attribute_id
     AND    UPPER(a.name)                   = UPPER(p_attribute)
     AND    i.m_attributevalue_id           = v.m_attributevalue_id(+);

   EXCEPTION
   WHEN NO_DATA_FOUND THEN
        v_attribute_id       := NULL;
        v_attributevalue_id  := NULL;
   WHEN TOO_MANY_ROWS THEN
        mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,2,'F','mod_utils.setattr','TOOMANY:'||p_attrsetinst_id||':'||p_source_type||':'||p_source_id||':'||p_attribute);
        RAISE g_trapped;
     
   END;

   -- Already exists so update it
   IF v_attribute_id IS NOT NULL THEN

     IF v_attributevalue_id IS NULL THEN

        -- It is a simple value then set it
        UPDATE m_attributeinstance i
        SET    i.value                      = p_new_value,
               i.updated                    = SYSDATE,
               i.updatedby                  = 0
        WHERE  i.m_attributesetinstance_id  = p_attrsetinst_id
        AND    i.m_attribute_id             = v_attribute_id;

     ELSE

     -- Get the id of the new value and set that
        BEGIN

            SELECT v.m_attributevalue_id
            INTO   v_attributevalue_id
            FROM   m_attributevalue v
            WHERE  v.m_attribute_id         = v_attribute_id
            AND    UPPER(v.description)     = UPPER(p_new_value);             
            
            UPDATE m_attributeinstance i
            SET    i.m_attributevalue_id        = v_attributevalue_id,
                   i.updated                    = SYSDATE,
                   i.updatedby                  = 0
            WHERE  i.m_attributesetinstance_id  = p_attrsetinst_id
            AND    i.m_attribute_id             = v_attribute_id;
            
        EXCEPTION
        WHEN NO_DATA_FOUND THEN
             mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,2,'F','mod_utils.setval.updgetval','NOROWS:'||v_attributevalue_id||':'||p_new_value);
             RAISE g_trapped;
        WHEN TOO_MANY_ROWS THEN
             mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,2,'F','mod_utils.setval.updgetval','TOOMANY:'||v_attributevalue_id||':'||p_new_value);
             RAISE g_trapped;
        END;    
        
     END IF;

   -- Does not exists so create it.
   ELSE      

        -- Get some attribute details
        BEGIN
            SELECT a.m_attribute_id,
                   a.attributevaluetype
            INTO   v_attribute_id,
                   v_valuetype
            FROM   m_attribute a
            WHERE  UPPER(a.name)  = UPPER(p_attribute);
        EXCEPTION
        WHEN NO_DATA_FOUND THEN
             mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,2,'F','mod_utils.setval.insgetattr','NOROWS:'||p_attribute||':'||p_new_value);
             RAISE g_trapped;
        WHEN TOO_MANY_ROWS THEN
             mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,2,'F','mod_utils.setval.insgetattr','TOOMANY:'||p_attribute||':'||p_new_value);
             RAISE g_trapped;
        END;    

        -- If attribute is value based then get attribute value id        
        IF v_valuetype = 'L' THEN

          BEGIN
            SELECT v.m_attributevalue_id
            INTO   v_attributevalue_id
            FROM   m_attributevalue v
            WHERE  v.m_attribute_id         = v_attribute_id
            AND    UPPER(v.description)     = UPPER(p_new_value);             
          EXCEPTION
          WHEN NO_DATA_FOUND THEN
             mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,2,'F','mod_utils.setval.insgetval','NOROWS:'||v_attributevalue_id||':'||p_new_value);
             RAISE g_trapped;
          WHEN TOO_MANY_ROWS THEN
             mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,2,'F','mod_utils.setval.insgetval','TOOMANY:'||v_attributevalue_id||':'||p_new_value);
             RAISE g_trapped;
          END;    
          v_new_value := NULL;

        ELSE

          v_new_value             := p_new_value;
          v_attributevalue_id     := NULL;

        END IF;

        -- Attribte did not exist in set so Insert New attribute instance
        INSERT INTO m_attributeinstance(
         m_attributesetinstance_id, 
         m_attribute_id, 
         ad_client_id, 
         ad_org_id, 
         isactive,
         created,
         createdby,
         updated, 
         updatedby,
         m_attributevalue_id,
         value)
        VALUES(
         p_attrsetinst_id,
         v_attribute_id,
         p_client_id,
         p_org_id,
         'Y',
         SYSDATE,
         0,
         SYSDATE,
         0,
         v_attributevalue_id,
         v_new_value);         

   END IF;
   
EXCEPTION 
WHEN OTHERS THEN
     g_err := SQLCODE;
     g_errm := SQLERRM;
     mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,1,'F','mod_utils.setattr','UNKNOWN:'||g_err||':'||g_errm||':'||p_attrsetinst_id||':'||p_source_type||':'||p_source_id||':'||p_attribute);
     RAISE;

END setattr;

--================================================================================================
-- Create new attribute Set Instance
--================================================================================================

PROCEDURE newsetinst(
 p_debug                     NUMBER,
 p_batch_id                  IN NUMBER,
 p_attrsetinst_id            IN OUT NUMBER,
 p_client_id                 IN     NUMBER,
 p_org_id                    IN     NUMBER,
 p_source_type               IN     VARCHAR2,
 p_source_id                 IN     NUMBER,
 p_attrset_name              IN     VARCHAR2
)
IS

v_attrset_id                 NUMBER;

BEGIN

g_debug  := p_debug;

   -- Set attribure set id
   BEGIN
       SELECT s.m_attributeset_id
       INTO   v_attrset_id
       FROM   m_attributeset s
       WHERE  UPPER(s.name)       = UPPER(p_attrset_name);
   EXCEPTION
   WHEN NO_DATA_FOUND THEN
        mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,2,'F','mod_utils.newsetinst.getsetid','NOROWS:'||p_attrset_name);
        RAISE g_trapped;
   WHEN TOO_MANY_ROWS THEN
        mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,2,'F','mod_utils.newsetinst.getsetid','TOOMANY:'||p_attrset_name);
        RAISE g_trapped;
   END;    

   -- Get next id
   ad_sequence_next('M_AttributeSetInstance',1000000,p_attrsetinst_id);

   -- Insert new set
   INSERT INTO m_attributesetinstance(
         m_attributesetinstance_id,
         ad_client_id, 
         ad_org_id, 
         isactive,
         created,
         createdby,
         updated, 
         updatedby,
         m_attributeset_id,
         description)
        VALUES(
         p_attrsetinst_id,
         p_client_id,
         p_org_id,
         'Y',
         SYSDATE,
         0,
         SYSDATE,
         0,
         v_attrset_id,
         NULL);         

EXCEPTION 
WHEN OTHERS THEN
     g_err := SQLCODE;
     g_errm := SQLERRM;
     mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,1,'F','mod_utils.newsetinst','UNKNOWN:'||g_err||':'||g_errm||':'||p_attrset_name);
     RAISE;

END newsetinst;

--================================================================================================
-- Create new attribute Set Instance
--================================================================================================

PROCEDURE setinstdescr(
 p_client_id                 NUMBER,
 p_org_id                    NUMBER,
 p_debug                     NUMBER,
 p_batch_id                  IN NUMBER,
 p_attrsetinst_id            IN NUMBER)
IS

CURSOR attr_c IS
SELECT NVL(v.description,a.value) descr
FROM   m_attributeinstance a,
       m_attributevalue v,
       m_attributesetinstance i,
       m_attributeuse u
WHERE  a.m_attributesetinstance_id     = p_attrsetinst_id
AND    a.m_attributesetinstance_id     = i.m_attributesetinstance_id
AND    i.m_attributeset_id             = u.m_attributeset_id
AND    a.m_attribute_id                = u.m_attribute_id
AND    a.m_attributevalue_id           = v.m_attributevalue_id(+)
ORDER BY u.seqno;

v_description                VARCHAR2(255);
v_seperator                  VARCHAR2(1);

BEGIN

g_debug  := p_debug;

v_description := '';
v_seperator := '';

FOR attr IN attr_c LOOP

    v_description := v_description || v_seperator || attr.descr;
    v_seperator := '_';
    
END LOOP;

UPDATE m_attributesetinstance i
SET    i.description = v_description
WHERE  i.m_attributesetinstance_id = p_attrsetinst_id;

EXCEPTION 
WHEN OTHERS THEN
     g_err := SQLCODE;
     g_errm := SQLERRM;
     mod_utils.debug(p_client_id, p_org_id, g_debug,p_batch_id,1,'F','mod_utils.setinstdescr','UNKNOWN:'||g_err||':'||g_errm);
     RAISE;

END setinstdescr;

END mod_utils;
/
