ALTER TABLE `consultingtypeservice`.`topic`
    MODIFY `name` varchar(225) COLLATE 'utf8_unicode_ci' NULL,
    MODIFY `description` varchar(225) COLLATE 'utf8_unicode_ci' NULL;

UPDATE `consultingtypeservice`.`topic`
SET name = concat('{"de": "', name, '"}');
UPDATE `consultingtypeservice`.`topic`
SET description = concat('{"de": "', description, '"}');
