package com.soloSavings.repository;

import com.soloSavings.model.Transaction;
import com.soloSavings.model.helper.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * Copyright (c) 2023 Team 2 - SoloSavings
 * Boston University MET CS 673 - Software Engineering
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Team 2 - SoloSavings Application
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    //Expenses

    @Query("SELECT t FROM Transaction t WHERE t.user_id = ?1 and t.transaction_type = ?2")
    List<Transaction> findByTransactionType(Integer user_id, TransactionType transaction_type);

    //Income
    //List<Transaction> findByTransactionType(TransactionType transactionType);

}
