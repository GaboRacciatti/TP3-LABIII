package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoMoneda;
import ar.edu.utn.frbb.tup.model.TipoPersona;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.tipoDeCuentaSoportada;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CuentaServiceTest {

    @Mock
    private CuentaDao cuentaDao;

    @Mock
    private ClienteDao clienteDao;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private CuentaService cuentaService;


    @BeforeAll
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CuentaDao cuentaDao = new CuentaDao();
        cuentaService = new CuentaService();
    }

    private Cliente CrearCliente() throws ClienteAlreadyExistsException {
        Cliente gabo = new Cliente();
        gabo.setDni(26456439);
        gabo.setNombre("gabo");
        gabo.setApellido("racc");
        gabo.setFechaNacimiento(LocalDate.of(2005, 4,19));
        gabo.setTipoPersona(TipoPersona.PERSONA_FISICA);
        clienteService.darDeAltaCliente(gabo);
        return gabo;
    }
    private Cuenta CrearCuenta(Cliente gabo,TipoCuenta tipoCuenta, TipoMoneda tipoMoneda, double d) throws TipoCuentaAlreadyExistsException,  CuentaAlreadyExistsException, tipoDeCuentaSoportada { 
        Cuenta cuenta = new Cuenta();
        cuenta.setTitular(gabo);
        cuenta.setNumeroCuenta(12345678);
        cuenta.setMoneda(tipoMoneda);
        cuenta.setBalance(1000);
        cuenta.setTipoCuenta(tipoCuenta);
        //cuentaService.darDeAltaCuenta(cuenta, gabo.getDni());
        return cuenta;
    }

    @Test
    void testDarDeAltaCuentaExistente() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, tipoDeCuentaSoportada, ClienteAlreadyExistsException {
        Cliente gabo = CrearCliente();
        Cuenta cuenta = CrearCuenta(gabo, TipoCuenta.CAJA_AHORRO, TipoMoneda.PESOS, 1000.0);
        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(cuenta);  
    
        assertThrows(CuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuenta, gabo.getDni()));
        doThrow(CuentaAlreadyExistsException.class).when(cuentaDao).save(cuenta);
    }  

     

    @Test
    void testDarDeAltaCuentaNoSoportada() {
        Cuenta cuenta = new Cuenta(1234, TipoCuenta.CUENTA_CORRIENTE, TipoMoneda.DOLARES, 1000.0);

        assertThrows(UnsupportedOperationException.class, () -> cuentaService.darDeAltaCuenta(cuenta, 12345678));
    }

    @Test
    void testDarDeAltaCuentaClienteYaTieneCuentaTipo() throws TipoCuentaAlreadyExistsException, CuentaAlreadyExistsException, tipoDeCuentaSoportada, ClienteAlreadyExistsException {
        Cliente gabo = CrearCliente();
        Cuenta cuenta = CrearCuenta(gabo, TipoCuenta.CAJA_AHORRO, TipoMoneda.PESOS, 500000);

        when(clienteDao.find(gabo.getDni(), true)).thenReturn(gabo);

        clienteService.agregarCuenta(cuenta, gabo.getDni());

        Cuenta cuenta2 = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(50000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        assertThrows(TipoCuentaAlreadyExistsException.class, () -> clienteService.agregarCuenta(cuenta2, gabo.getDni()));
        verify(clienteDao, times(2)).save(gabo);

        assertEquals(1, gabo.getCuentas().size());
        assertEquals(gabo, cuenta.getTitular());
    }

    @Test
    void testDarDeAltaCuentaExitosa() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, tipoDeCuentaSoportada {
        Cuenta cuenta = new Cuenta(1234, TipoCuenta.CAJA_AHORRO, TipoMoneda.PESOS, 1000.0);

        cuentaService.darDeAltaCuenta(cuenta, 12345678);

        verify(cuentaDao, times(1)).save(cuenta);
    }
}
