from enum import Enum
from tempfile import mkstemp

CERT_START_LINE = "-----BEGIN CERTIFICATE-----"

def nonblank_lines(f):
    for l in f:
        line  = l.rstrip()
        if line:
            yield line                    
            

class VOMSHelperLibrary:
    """An helper library for VOMS"""

    ROBOT_LIBRARY_SCOPE = "GLOBAL"

    def extract_eec_from_proxy(self, proxy_file, dest=None):
        """Extracts the EEC certificate from a X.509 proxy chain and saves it 
        in the dest file, or, when dest is None, in a temporary file.
        
        Returns the tuple (eec, dest_file)
        """
        
        with open(proxy_file) as f:
            lines = [line for line in nonblank_lines(f)]
            
            last_cert_idx = len(lines) - 1 - lines[::-1].index(CERT_START_LINE)
            eec = '\n'.join(lines[last_cert_idx:])
            
            if dest is None:
                (fd, dest_file) = mkstemp()
            else:
                dest_file = dest

            with open(dest_file, 'w') as d:
                d.write(eec)
            
            return (eec,dest_file)