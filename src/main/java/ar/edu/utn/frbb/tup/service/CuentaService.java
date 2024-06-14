package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.tipoDeCuentaSoportada;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

//Generar casos de test para darDeAltaCuenta
    //    1 - cuenta existente
    //    2 - cuenta no soportada
    //    3 - cliente ya tiene cuenta de ese tipo
    //    4 - cuenta creada exitosamente

@Component
public class CuentaService {

    private final CuentaDao cuentaDao = new CuentaDao();    

    @Autowired
    private ClienteService clienteService;

    private static final Set<TipoCuenta> TIPOS_CUENTAS_SOPORTADAS = EnumSet.of(
            TipoCuenta.CAJA_AHORRO,
            TipoCuenta.CUENTA_CORRIENTE
    );

    public void darDeAltaCuenta(Cuenta cuenta, long dniTitular) throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, tipoDeCuentaSoportada {
        //    1 - cuenta existente
        if (cuentaDao.find(cuenta.getNumeroCuenta()) != null) {
            throw new CuentaAlreadyExistsException("La cuenta " + cuenta.getNumeroCuenta() + " ya existe.");
        }
        //    2 - cuenta no soportada
        if (!tipoCuentaEstaSoportada(cuenta)) {
            throw new tipoDeCuentaSoportada("El tipo de cuenta " + cuenta.getTipoCuenta() + " no está soportado por el banco.");
        }
        //    3 - cliente ya tiene cuenta de ese tipo
        //if (clienteService.buscarClientePorDni(dniTitular).tieneCuenta(cuenta.getTipoCuenta(), cuenta.getMoneda())) {
          //  throw new TipoCuentaAlreadyExistsException("El cliente ya posee una cuenta de ese tipo y moneda");
        //}
        //    4 - cuenta creada exitosamente
        clienteService.agregarCuenta(cuenta, dniTitular);
        cuentaDao.save(cuenta);
    }

    public Cuenta find(long id) {
        return cuentaDao.find(id);
    }

    private boolean tipoCuentaEstaSoportada(Cuenta cuenta) {
        return TIPOS_CUENTAS_SOPORTADAS.contains(cuenta.getTipoCuenta());
    }
}
