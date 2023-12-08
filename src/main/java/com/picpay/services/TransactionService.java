package com.picpay.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.mapping.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.picpay.domain.transaction.Transaction;
import com.picpay.domain.user.User;
import com.picpay.dtos.TransactionDTO;
import com.picpay.repositories.TransactionRepository;


@Service
public class TransactionService {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository repository;

    private RestTemplate restTemplate;

    public void createTransaction(TransactionDTO transaction) throws Exception{
        User sender = this.userService.findUserById(transaction.senderId());
        User receiver = this.userService.findUserById(transaction.receiverId());

        userService.validateTransaction(sender, transaction.value());

        
        boolean isAuthorized = this.authorizeTransaction(sender, transaction.value());

        if(!isAuthorized){
            throw new Exception("Transação não autorizada!");
        }

        Transaction newTransation = new Transaction();
        newTransation.setAmount(transaction.value());
        newTransation.setSender(sender);
        newTransation.setReceiver(receiver);
        newTransation.setTimestamp(LocalDateTime.now());

        sender.setBalance(sender.getBalance().subtract(transaction.value()));
        receiver.setBalance(receiver.getBalance().add(transaction.value()));

        this.repository.save(newTransation);
        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);


    }

    public Boolean authorizeTransaction(User sender, BigDecimal value){
        ResponseEntity<Map> authorizationResponse = restTemplate.getForEntity("https://run.mocky.io/v3/5794d450-d2e2-4412-8131-73d0293ac1cc", Map.class);
        if(authorizationResponse.getStatusCode() == HttpStatus.OK){
            return true;
        } else {
            return false;
        }

     }

     

}
