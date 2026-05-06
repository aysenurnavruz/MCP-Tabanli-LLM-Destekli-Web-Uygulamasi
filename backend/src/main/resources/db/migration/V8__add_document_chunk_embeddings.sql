SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'document_chunks'
      AND COLUMN_NAME = 'embedding'
);

SET @ddl = IF(
    @column_exists = 0,
    'ALTER TABLE document_chunks ADD COLUMN embedding LONGTEXT NULL',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
