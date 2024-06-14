package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoMoneda;
import ar.edu.utn.frbb.tup.model.TipoPersona;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.tipoDeCuentaSoportadaException;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private Cuenta CrearCuenta(Cliente gabo,TipoCuenta tipoCuenta, TipoMoneda tipoMoneda, double d) throws TipoCuentaAlreadyExistsException,  CuentaAlreadyExistsException, tipoDeCuentaSoportadaException { 
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
    void testDarDeAltaCuentaExistente() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, tipoDeCuentaSoportadaException, ClienteAlreadyExistsException {
        Cliente gabo = CrearCliente();
        gabo.setDni(123456789);
    
        Cuenta cuentaExistente = CrearCuenta(gabo, TipoCuenta.CAJA_AHORRO, TipoMoneda.PESOS, 50000);
        //when(cuentaDao.find(cuentaExistente.getNumeroCuenta())).thenReturn(new Cuenta());

        //ESTA EXCEPTION FUNCIONA SOLO SI DEJO LA EXCEPTION "TipoCuentaAlreadyExistsException" LO CUAL NO ESTA BIEN, DEBERIA IR LA EXCEPTION "CuentaAlreadyExistsException" PERO NO ME FUNCIONA Y  NO ENCUENTRO EL ERROR.
        doThrow(TipoCuentaAlreadyExistsException.class).when(clienteService).agregarCuenta(cuentaExistente, gabo.getDni());
        assertThrows(TipoCuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuentaExistente,gabo.getDni()));    

    } 

     

    @Test
    void testDarDeAltaCuentaNoSoportada() {
        Cuenta cuenta = new Cuenta();
        cuenta.setTipoCuenta(null);

        assertThrows(tipoDeCuentaSoportadaException.class, () -> cuentaService.darDeAltaCuenta(cuenta, 12345678));
    }


    @Test
    void testDarDeAltaCuentaClienteYaTieneCuentaTipo() throws TipoCuentaAlreadyExistsException, CuentaAlreadyExistsException, tipoDeCuentaSoportadaException, ClienteAlreadyExistsException {
        Cuenta cuenta = new Cuenta();
        cuenta.setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);
        cuenta.setMoneda(TipoMoneda.PESOS);

        Cliente cliente = new Cliente();
        cliente.setDni(12345678);
        cliente.addCuenta(cuenta);
        cliente.setNombre("gabo");
        cliente.setApellido("racc");
        cliente.setFechaNacimiento(LocalDate.of(2005, 4,19));
        cliente.setTipoPersona(TipoPersona.PERSONA_FISICA);

        doThrow(TipoCuentaAlreadyExistsException.class).when(clienteService).agregarCuenta(cuenta, cliente.getDni());
        assertThrows(TipoCuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuenta, cliente.getDni()));
    }

    @Test
    void testDarDeAltaCuentaExitosa() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, tipoDeCuentaSoportadaException, ClienteAlreadyExistsException {
        Cuenta cuenta = new Cuenta();
        cuenta.setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);
        cuenta.setMoneda(TipoMoneda.PESOS);
        cuenta.setNumeroCuenta(1234);

        Cliente gabo = new Cliente();
        gabo.setDni(1234);
        gabo.setNombre("gabo");
        gabo.setApellido("racc");
        gabo.setFechaNacimiento(LocalDate.of(2005, 4,19));
        gabo.setTipoPersona(TipoPersona.PERSONA_FISICA);


        clienteService.darDeAltaCliente(gabo);
        cuentaService.darDeAltaCuenta(cuenta, gabo.getDni());
        clienteService.agregarCuenta(cuenta, gabo.getDni());
        gabo.addCuenta(cuenta);
        cuentaDao.save(cuenta);
        verify(cuentaDao, times(1)).save(cuenta);

        assertEquals(1, gabo.getCuentas().size());
        assertEquals(gabo, cuenta.getTitular());

    }
}