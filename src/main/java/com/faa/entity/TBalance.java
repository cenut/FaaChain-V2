package com.faa.entity;

import com.faa.chain.token.CoinsBaseUnits;
import com.faa.chain.token.FaaMain;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="balance")
public class TBalance {

    @Id
    @GeneratedValue
    private Integer id;
    private String address;
    private String balance;
    private BigDecimal balancef;
    private int status;
    private Boolean enable;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String ballance) {
        this.balance = ballance;
    }

    public BigDecimal getBalancef() {
        return balancef;
    }

    public void setBalancef(BigDecimal balancef) {
        this.balancef = balancef;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public void syncBalancef(){
        this.balancef = CoinsBaseUnits.toHumanUnit(this.balance, FaaMain.get());
    }
}
