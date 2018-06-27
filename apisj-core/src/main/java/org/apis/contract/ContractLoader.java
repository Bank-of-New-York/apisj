package org.apis.contract;

import com.google.gson.JsonArray;
import org.apis.core.CallTransaction;
import org.apis.core.Transaction;
import org.apis.solidity.compiler.CompilationResult;
import org.apis.solidity.compiler.SolidityCompiler;
import org.apis.util.ByteUtil;
import org.apis.util.blockchain.StandaloneBlockchain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;

public class ContractLoader {
    private static Logger logger = LoggerFactory.getLogger("ContractLoader");

    public static final int CONTRACT_ADDRESS_MASKING = 0;
    public static final int CONTRACT_GATE_KEEPER = 1;
    public static final int CONTRACT_MINERAL_CHARGE = 2;


    public static String loadContractSource(int contractType) throws RuntimeException {
        String contractFileName;
        switch(contractType) {
            case CONTRACT_ADDRESS_MASKING:
                contractFileName = "AddressMasking.sol";
                break;
            case CONTRACT_GATE_KEEPER:
                contractFileName = "";
                break;
            case CONTRACT_MINERAL_CHARGE:
                contractFileName = "";
                break;
            default:
                return null;
        }

        URL contractUrl = ContractLoader.class.getClassLoader().getResource("contract/" + contractFileName);
        if(contractUrl == null) {
            return null;
        }

        // #1 try to find genesis at passed location
        try (InputStream is = new FileInputStream(new File(contractUrl.toURI()))) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                out.append(line);
            }

            return out.toString();
        } catch (Exception e) {
            logger.error("Problem loading contract file from " + contractFileName);
        }

        return null;
    }


    public static Transaction getAddressMaskingContractCreation(BigInteger nonce, int chainId) {
        try {
            String contractSource = loadContractSource(CONTRACT_ADDRESS_MASKING);
            if(contractSource == null) {
                return null;
            }
            SolidityCompiler.Result result = SolidityCompiler.compile(contractSource.getBytes(), true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);

            if(result.isFailed()) {
                logger.error("Contract compilation failed : \n" + result.errors);
                return null;
            }

            CompilationResult res = CompilationResult.parse(result.output);
            if(res.contracts.isEmpty()) {
                logger.error("Compilation failed, no binary returned:\n" + result.errors);
                return null;
            }

            CompilationResult.ContractMetadata metadata = null;

            for(String contract : res.contracts.keySet()) {
                if(contract.equalsIgnoreCase("addressmasking")) {
                    metadata = res.contracts.get(contract);
                    break;
                }
            }
            if(metadata == null) {
                return null;
            }

            CallTransaction.Contract cont = new CallTransaction.Contract(metadata.solInterface);

            //TODO 생성자 인수들을 입력해야 함.
            byte[] initParams = cont.getConstructor().encodeArguments();

            byte[] data = ByteUtil.merge(Hex.decode(metadata.bin), initParams);

            //CompilationResult.ContractMetadata metadata = res.contracts.values().iterator().next();
            if(metadata.bin == null || metadata.bin.isEmpty()) {
                logger.error("Compilation failed, no binary returned:\n" + result.errors);
                return null;
            }



            return new Transaction(
                    ByteUtil.bigIntegerToBytes(nonce),
                    ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L),
                    ByteUtil.longToBytesNoLeadZeroes(30_000_000),
                    new byte[0],
                    ByteUtil.longToBytesNoLeadZeroes(0),
                    data,
                    chainId
            );

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}