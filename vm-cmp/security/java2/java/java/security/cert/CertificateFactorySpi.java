/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/


package java.security.cert;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @version $Id: CertificateFactorySpi.java,v 1.3 2006/04/18 11:35:28 cvs Exp $
 *
 */

public abstract class CertificateFactorySpi {

  public CertificateFactorySpi(){}

  public abstract Certificate engineGenerateCertificate(InputStream inStream) throws CertificateException;
  public abstract Collection engineGenerateCertificates(InputStream inStream) throws CertificateException;
  public abstract CRL engineGenerateCRL(InputStream inStream) throws CRLException;
  public abstract Collection engineGenerateCRLs(InputStream inStream) throws CRLException;

  public Iterator engineGetCertPathEncodings() {
    throw new UnsupportedOperationException("new in 1.4 Api");        
  }

  public CertPath engineGenerateCertPath(InputStream inStream,
      String encoding) throws CertificateException {
    
    throw new UnsupportedOperationException("new in 1.4 Api");            
  }

  public CertPath engineGenerateCertPath(InputStream stream) 
      throws CertificateException {
    
    throw new UnsupportedOperationException("new in 1.4 Api");          
  }

  public CertPath engineGenerateCertPath(List certificates)
      throws CertificateException {
    
    throw new UnsupportedOperationException("new in 1.4 Api");          
  }
}