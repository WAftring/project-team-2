package com.soloSavings.integration;

import com.soloSavings.exceptions.TransactionException;
import com.soloSavings.model.Login;
import com.soloSavings.model.Transaction;
import com.soloSavings.model.User;
import com.soloSavings.model.helper.TransactionType;
import com.soloSavings.repository.TransactionRepository;
import com.soloSavings.repository.UserRepository;
import com.soloSavings.service.TransactionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@Profile("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRepository transactionRepository;
    private User commonUser;
    HttpHeaders headers;
    @BeforeAll
    public void setUpUserAuth(){
        String commonUserEmail = "common@gmail.com";
        commonUser = new User(null,"common",commonUserEmail,"Password1",LocalDate.now(),0.0,LocalDate.now());
        restTemplate.postForEntity("/api/register", commonUser, String.class);

        Login login = new Login(commonUser.getUsername(),commonUser.getPassword_hash());

        ResponseEntity<String> loginResponse = restTemplate.postForEntity("/api/login", login, String.class);
        headers = new HttpHeaders();
        headers.setBearerAuth(Objects.requireNonNull(loginResponse.getBody()));

    }
    @BeforeEach
    public void setup(){
        commonUser = userRepository.findByUsername(commonUser.getUsername());
        commonUser.setBalance_amount(1000.00);
        userRepository.save(commonUser);
    }

    @Test
    public void testAddIncomeTransaction() {
        Transaction transaction = new Transaction(null,commonUser.getUser_id(),"", TransactionType.CREDIT,100.0, LocalDate.now());
        Double expectedIncome = commonUser.getBalance_amount() + 100.0;

        HttpEntity<Transaction> request = new HttpEntity<>(transaction, headers);
        ResponseEntity<Double> response = restTemplate.postForEntity("/api/transaction/add", request, Double.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedIncome);
    }

    @Test
    public void testAddIncomeTransactionInvalidAmount() {
        Transaction transaction = new Transaction(null,commonUser.getUser_id(),"", TransactionType.CREDIT,-100.0, LocalDate.now());

        HttpEntity<Transaction> request = new HttpEntity<>(transaction, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/transaction/add", request, String.class, commonUser.getUser_id());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isEqualTo("Invalid transaction amount, Please input correct transaction amount!");
    }

    @Test
    public void testAddExpenseTransaction() {
        Transaction transaction = new Transaction(null,commonUser.getUser_id(),"", TransactionType.DEBIT,100.0, LocalDate.now());
        Double expectedExpense = commonUser.getBalance_amount() - 100.0;

        HttpEntity<Transaction> request = new HttpEntity<>(transaction, headers);
        ResponseEntity<Double> response = restTemplate.postForEntity("/api/transaction/add", request, Double.class, commonUser.getUser_id());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedExpense);
    }
    @Test
    public void testAddExpenseTransactionInvalidAmount() {
        Double invalidExpense = commonUser.getBalance_amount() + 1;
        Transaction transaction = new Transaction(null,commonUser.getUser_id(),"", TransactionType.DEBIT,invalidExpense, LocalDate.now());

        HttpEntity<Transaction> request = new HttpEntity<>(transaction, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/transaction/add", request, String.class, commonUser.getUser_id());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isEqualTo("Invalid transaction amount, Please input correct transaction amount!");
    }

    @Test
    public void testGetTransactionByTypeDebit() {
        Transaction tran1 = new Transaction(null,commonUser.getUser_id(),"", TransactionType.DEBIT,150.0, LocalDate.now());
        Transaction tran2 = new Transaction(null,commonUser.getUser_id(),"", TransactionType.DEBIT,150.0, LocalDate.now());
        Transaction tran3 = new Transaction(null,commonUser.getUser_id(),"", TransactionType.CREDIT,150.0, LocalDate.now());
        transactionRepository.save(tran1);
        transactionRepository.save(tran2);
        transactionRepository.save(tran3);
        Integer expectedNumDebit = 2;

        HttpEntity<Transaction> request = new HttpEntity<>(headers);
        ResponseEntity<List> response = restTemplate.exchange("/api/transaction/DEBIT", HttpMethod.GET,request,List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(expectedNumDebit);
    }

    @Test
    public void testGetTransactionByTypeCredit() throws TransactionException {
        int initialNumCreditTrans = transactionService.getTransactionsByType(commonUser.getUser_id(),"CREDIT").size();
        Transaction tran1 = new Transaction(null,commonUser.getUser_id(),"", TransactionType.CREDIT,150.0, null);
        Transaction tran2 = new Transaction(null,commonUser.getUser_id(),"", TransactionType.CREDIT,150.0, null);
        Transaction tran3 = new Transaction(null,commonUser.getUser_id(),"", TransactionType.DEBIT,150.0, null);
        transactionRepository.save(tran1);
        transactionRepository.save(tran2);
        transactionRepository.save(tran3);
        Integer expectedNumCredit = initialNumCreditTrans + 2;

        HttpEntity<Transaction> request = new HttpEntity<>(headers);
        ResponseEntity<List> response = restTemplate.exchange("/api/transaction/CREDIT", HttpMethod.GET,request,List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(expectedNumCredit);
    }
    @Test
    public void testGetTransactionByTypeWithInvalidType() {
        String invalidTransactionType = "WRONG";

        HttpEntity<Transaction> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/api/transaction/"+invalidTransactionType, HttpMethod.GET,request,String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void testGetThisMonthExpense() throws TransactionException {
        Double initThisMonthExpense = transactionService.getThisMonthExpense(commonUser.getUser_id());
        Transaction tran1 = new Transaction(null,commonUser.getUser_id(),"", TransactionType.DEBIT,150.0, LocalDate.now());
        Transaction tran2 = new Transaction(null,commonUser.getUser_id(),"", TransactionType.DEBIT,150.0, null);
        transactionRepository.save(tran1);
        transactionRepository.save(tran2);
        Double expectedExpense = initThisMonthExpense + tran1.getAmount();

        HttpEntity<Transaction> request = new HttpEntity<>(headers);
        ResponseEntity<Double> response = restTemplate.exchange("/api/transaction/monthly/expense", HttpMethod.GET,request,Double.class);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedExpense);
    }

    @Test
    public void testGetThisMonthIncome() throws TransactionException {
        Double initThisMonthIncome = transactionService.getThisMonthIncome(commonUser.getUser_id());
        Transaction tran1 = new Transaction(null,commonUser.getUser_id(),"", TransactionType.CREDIT,150.0, LocalDate.now());
        Transaction tran2 = new Transaction(null,commonUser.getUser_id(),"", TransactionType.CREDIT,150.0, null);
        Transaction tran3 = new Transaction(null,commonUser.getUser_id(),"", TransactionType.CREDIT,100.0, LocalDate.now());
        transactionRepository.save(tran1);
        transactionRepository.save(tran2);
        transactionRepository.save(tran3);
        Double expectedIncome = initThisMonthIncome+tran1.getAmount()+tran3.getAmount();

        HttpEntity<Transaction> request = new HttpEntity<>(headers);
        ResponseEntity<Double> response = restTemplate.exchange("/api/transaction/monthly/income", HttpMethod.GET,request,Double.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedIncome);
    }
}
