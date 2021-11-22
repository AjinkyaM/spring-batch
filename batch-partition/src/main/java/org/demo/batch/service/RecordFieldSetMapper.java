package org.demo.batch.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.demo.batch.model.User;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class RecordFieldSetMapper implements FieldSetMapper<User> {

	@Override
	public User mapFieldSet(FieldSet fieldSet) throws BindException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyy");

		User user = new User();
        user.setUsername(fieldSet.readString("username"));
        user.setUserId(fieldSet.readInt("userid"));
        user.setAmount(fieldSet.readDouble(3));

        // Converting the date
        String dateString = fieldSet.readString(2);
        user.setTransactionDate(LocalDate.parse(dateString, formatter).atStartOfDay());

        return user;
	}

}
