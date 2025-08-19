package pl.eurokawa.file.conversion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NameConversionServiceImplTest {

    @InjectMocks
    NameConversionServiceImpl service;

    @Test
    void generateSecureFileName_workingTest(){
        String fileName = "test123!@";
        String secureFileName = service.generateSecureFileName(fileName);

        assertTrue(secureFileName.endsWith("test123"));
        assertTrue(secureFileName.length() > 36);
    }

    @Test
    void generateSecureFileName_spaceShouldBeUnderscore(){
        String fileName = "h e l l o";
        String secureFileName = service.generateSecureFileName(fileName);

        assertTrue(secureFileName.endsWith("h_e_l_l_o"));
    }

    @Test
    void generateSecureFileName_UUIDbeforeFileName(){
        String fileName = "a.txt";
        String secureFileName = service.generateSecureFileName(fileName);

        assertTrue(secureFileName.contains("_a.txt"));
        assertEquals(36 + 1 + 5, secureFileName.length());
    }

    @Test
    void generateSecureFileName_deleteUnwantedChars(){
        String fileName = "i Love !@#$%^&*()Programming";
        String secureFileName = service.generateSecureFileName(fileName);

        assertTrue(secureFileName.endsWith("i_love_programming"));
    }

    @Test
    void generateSecureFileName_toLowerCaseTest(){
        String fileName = "IWANTLOWERCASE";
        String secureFileName = service.generateSecureFileName(fileName);

        assertTrue(secureFileName.endsWith("iwantlowercase"));
    }

    @Test
    void generateSecureFileName_fileNameIsNull(){
        String fileName = null;

        assertThrows(NullPointerException.class, () -> service.generateSecureFileName(fileName));
    }
}