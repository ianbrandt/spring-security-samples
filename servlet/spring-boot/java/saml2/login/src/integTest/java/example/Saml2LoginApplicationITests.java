/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class Saml2LoginApplicationITests {

	static final String SIGNED_RESPONSE = "PHNhbWxwOlJlc3BvbnNlIHhtbG5zOnNhbWxwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiIHhtbG5zOnNhbWw9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIElEPSJfY2UyYjU4NTVjZjU5YmEyNDc4OTUyOGRkOGQzZDcyOGRiMGViZjNlNzNiIiBWZXJzaW9uPSIyLjAiIElzc3VlSW5zdGFudD0iMjAyMS0wMS0yMFQwMTowMzoyNFoiIERlc3RpbmF0aW9uPSJodHRwOi8vbG9jYWxob3N0OjgwODAvbG9naW4vc2FtbDIvc3NvL29uZSIgSW5SZXNwb25zZVRvPSJBUlFjOThmMjAwLWRjZjctNGRmNC1hNTIyLTA3MjA2MjA4YjA3ZCI+PHNhbWw6SXNzdWVyPmh0dHBzOi8vc2ltcGxlc2FtbC1mb3Itc3ByaW5nLXNhbWwuYXBwcy5wY2ZvbmUuaW8vc2FtbDIvaWRwL21ldGFkYXRhLnBocDwvc2FtbDpJc3N1ZXI+PGRzOlNpZ25hdHVyZSB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+CiAgPGRzOlNpZ25lZEluZm8+PGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KICAgIDxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNyc2Etc2hhMjU2Ii8+CiAgPGRzOlJlZmVyZW5jZSBVUkk9IiNfY2UyYjU4NTVjZjU5YmEyNDc4OTUyOGRkOGQzZDcyOGRiMGViZjNlNzNiIj48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+TXJUNS8wdTRScTl3QmMvejFQd2FrNURXZm1xOGlOVk52NldHZWFnZUVzUT08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+WWg1UFE5cFVBbkkvOW5tNzgxZ040bThTS0U4T1VRTDVOMlI3ZklPZmtHVmMzRjhzNlJlR3hRNWFneUFXYnQzUDRwQ3RWeGtqbk4rMk5KeUw4QmhRMHN0dEovb2JFTHJGUldLemYyYUJaS2NCN0JHTFNtRXdoUFE3N3BHL0psMjBhaDQyaGRyWFc3TE9Ob0VZOHMyY093dm16NkQ2bW9YQWprMHV2UEVTNjhUVndxU2VmT3JwNXV0QmRSQUt6cUJRQ2NQWFJCdnB5NWJ3QkpDL2RKNE5QLzJpalE3N2I3eWhvVDQ0R21hSUduSGo0YVFaeG9kY1JuNU9oQ2hYRk4ydUk2YW1mT0ZYOThjUXZ5KzhDWm9YYUZRMnJmT2dPbGdzbmNGWGMwaXhYK05MSjlvSlJWT2hxRVpjY2JoZ3hPM2hpQ2UwemRuZHloVWxpaGMwdFU2OVlBPT08L2RzOlNpZ25hdHVyZVZhbHVlPgo8ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlFRXpDQ0F2dWdBd0lCQWdJSkFJYzFxekxydis1bk1BMEdDU3FHU0liM0RRRUJDd1VBTUlHZk1Rc3dDUVlEVlFRR0V3SlZVekVMTUFrR0ExVUVDQXdDUTA4eEZEQVNCZ05WQkFjTUMwTmhjM1JzWlNCU2IyTnJNUnd3R2dZRFZRUUtEQk5UWVcxc0lGUmxjM1JwYm1jZ1UyVnlkbVZ5TVFzd0NRWURWUVFMREFKSlZERWdNQjRHQTFVRUF3d1hjMmx0Y0d4bGMyRnRiSEJvY0M1alptRndjSE11YVc4eElEQWVCZ2txaGtpRzl3MEJDUUVXRVdab1lXNXBhMEJ3YVhadmRHRnNMbWx2TUI0WERURTFNREl5TXpJeU5EVXdNMW9YRFRJMU1ESXlNakl5TkRVd00xb3dnWjh4Q3pBSkJnTlZCQVlUQWxWVE1Rc3dDUVlEVlFRSURBSkRUekVVTUJJR0ExVUVCd3dMUTJGemRHeGxJRkp2WTJzeEhEQWFCZ05WQkFvTUUxTmhiV3dnVkdWemRHbHVaeUJUWlhKMlpYSXhDekFKQmdOVkJBc01Ba2xVTVNBd0hnWURWUVFEREJkemFXMXdiR1Z6WVcxc2NHaHdMbU5tWVhCd2N5NXBiekVnTUI0R0NTcUdTSWIzRFFFSkFSWVJabWhoYm1sclFIQnBkbTkwWVd3dWFXOHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDNGNuNjJFMXhMcXBOMzRQbWJyS0Jia09YRmp6V2dKOWIrcFh1YVJmdDZBMzM5dXVJUWVvZUg1cWVTS1JWVGwzMkwwZ2R6Mlppdkx3WlhXK2NxdmZ0VlcxdHZFSHZ6SkZ5eGVUVzNmQ1VlQ1FzZWJMbkEycVJhMDdSa3hUbzZOZjI0NG1XV1JEb2Rjb0hFZkRVU2J4ZlRaNklFeFNvalNJVTJSbkQ2V2xsWVdGZEQxR0ZwQkpPbVFCOHJBYzh3SklCZEhGZFFuWDhUdGw3aFo2cnRncUVZTXpZVk11SjJGMnIxSFNVMXpTQXZ3cGRZUDZyUkdGUkpFZmRBOW1tM1dLZk5MU2M1Y2xqejBYL1RYeTB2VmxBVjk1bDlxY2ZGelBtcmtOSXN0OUZaU3dwdkI0OUx5QVZrZTA0RlFQUHdMZ1ZINGdwaGlKSDNqdlo3SStKNWxTOFZBZ01CQUFHalVEQk9NQjBHQTFVZERnUVdCQlRUeVA2Q2M1SGxCSjUrdWNWQ3dHYzVvZ0tOR3pBZkJnTlZIU01FR0RBV2dCVFR5UDZDYzVIbEJKNSt1Y1ZDd0djNW9nS05HekFNQmdOVkhSTUVCVEFEQVFIL01BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQXZNUzRFUWVQL2lwVjRqT0c1bE82L3RZQ2IvaUplQWR1T25SaGtKazBEYlgzMjlsRExaaFRUTC94L3cvOW11Q1ZjdkxyekVwNlBOK1ZXZnc1RTVGV3RaTjB5aEd0UDlSK3ZabnJWK29jMnpHRCtubzEveVNGT2UzRWlKQ081ZGVoeEtqWUVtQlJ2NXNVL0xaRktacG96S04vQk1FYTZDcUx1eGJ6Yjd5a3hWcjdFVkZYd2x0UHh6RTlUbUw5T0FDTk55RjVlSkhXTVJNbGxhclV2a2NYbGg0cHV4NGtzOWU2elY5RFFCeTJ6ZHM5ZjFJM3F4ZzBlWDZKbkdyWGkvWmlDVCtsSmdWZTNaRlhpZWppTEFpS0IwNHNYVzN0aTBMVzNseDEzWTFZbFE0L3RscGdUZ2ZJSnhLVjZueVBpTG9LMG55d2JNZCt2cEFpckR0Mk9jK2hrPC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L2RzOktleUluZm8+PC9kczpTaWduYXR1cmU+PHNhbWxwOlN0YXR1cz48c2FtbHA6U3RhdHVzQ29kZSBWYWx1ZT0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnN0YXR1czpTdWNjZXNzIi8+PC9zYW1scDpTdGF0dXM+PHNhbWw6QXNzZXJ0aW9uIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgSUQ9Il9jYjYzYmMzNmMyYzAzYjRlMWJjZDViMWIwY2MyZTE2NWQwNDQ1NDZlODgiIFZlcnNpb249IjIuMCIgSXNzdWVJbnN0YW50PSIyMDIxLTAxLTIwVDAxOjAzOjI0WiI+PHNhbWw6SXNzdWVyPmh0dHBzOi8vc2ltcGxlc2FtbC1mb3Itc3ByaW5nLXNhbWwuYXBwcy5wY2ZvbmUuaW8vc2FtbDIvaWRwL21ldGFkYXRhLnBocDwvc2FtbDpJc3N1ZXI+PGRzOlNpZ25hdHVyZSB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+CiAgPGRzOlNpZ25lZEluZm8+PGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KICAgIDxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNyc2Etc2hhMjU2Ii8+CiAgPGRzOlJlZmVyZW5jZSBVUkk9IiNfY2I2M2JjMzZjMmMwM2I0ZTFiY2Q1YjFiMGNjMmUxNjVkMDQ0NTQ2ZTg4Ij48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+VWFtN2NHVGlCd2xuRDBJdGd5aU5KVjN2Z0NPNytZZkRxSWJrWERkR3hrQT08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+RkhpWUpETDlKTXM1Y2V5WXhUVVgrUndEQm45RFYzVE81dDFham4raGFtb1c2MUpBY0JaNjEwUHpYMzN3alA3Mk1kYmdDWnR5ZmNrSktZUUpPT0szRkxLTkJLQkphOTNsSS9rZWZjTXRTUGxBU2hESm9ydmU0U0tWa29WbzZLVnB0eC9OTnowRkhJNURFZTZiUUVjZWFiNERVNDFVdEpQMHUyWm16ejVjNC83VzhLdmt6MkxMbXhWZlE3Q2todmgvNzBhWHlkWVBVRml3bE4vV1lTV3JYVU9oOXNFTDFiZGVlQzFkYnpaeVdNNldnSkdRMUpJblBnSGd0YTlxMU96eGliOFlLRXpQSUMzVEZldkU1Y0phMFQvd1NzOVIxN0JSR09OclhTTWQvRCt4YkY0Z3lIYW5EZFlOYVN2TzdIS2p4bzRwYk1aY05peDhMTkVYZGtiZEx3PT08L2RzOlNpZ25hdHVyZVZhbHVlPgo8ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlFRXpDQ0F2dWdBd0lCQWdJSkFJYzFxekxydis1bk1BMEdDU3FHU0liM0RRRUJDd1VBTUlHZk1Rc3dDUVlEVlFRR0V3SlZVekVMTUFrR0ExVUVDQXdDUTA4eEZEQVNCZ05WQkFjTUMwTmhjM1JzWlNCU2IyTnJNUnd3R2dZRFZRUUtEQk5UWVcxc0lGUmxjM1JwYm1jZ1UyVnlkbVZ5TVFzd0NRWURWUVFMREFKSlZERWdNQjRHQTFVRUF3d1hjMmx0Y0d4bGMyRnRiSEJvY0M1alptRndjSE11YVc4eElEQWVCZ2txaGtpRzl3MEJDUUVXRVdab1lXNXBhMEJ3YVhadmRHRnNMbWx2TUI0WERURTFNREl5TXpJeU5EVXdNMW9YRFRJMU1ESXlNakl5TkRVd00xb3dnWjh4Q3pBSkJnTlZCQVlUQWxWVE1Rc3dDUVlEVlFRSURBSkRUekVVTUJJR0ExVUVCd3dMUTJGemRHeGxJRkp2WTJzeEhEQWFCZ05WQkFvTUUxTmhiV3dnVkdWemRHbHVaeUJUWlhKMlpYSXhDekFKQmdOVkJBc01Ba2xVTVNBd0hnWURWUVFEREJkemFXMXdiR1Z6WVcxc2NHaHdMbU5tWVhCd2N5NXBiekVnTUI0R0NTcUdTSWIzRFFFSkFSWVJabWhoYm1sclFIQnBkbTkwWVd3dWFXOHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDNGNuNjJFMXhMcXBOMzRQbWJyS0Jia09YRmp6V2dKOWIrcFh1YVJmdDZBMzM5dXVJUWVvZUg1cWVTS1JWVGwzMkwwZ2R6Mlppdkx3WlhXK2NxdmZ0VlcxdHZFSHZ6SkZ5eGVUVzNmQ1VlQ1FzZWJMbkEycVJhMDdSa3hUbzZOZjI0NG1XV1JEb2Rjb0hFZkRVU2J4ZlRaNklFeFNvalNJVTJSbkQ2V2xsWVdGZEQxR0ZwQkpPbVFCOHJBYzh3SklCZEhGZFFuWDhUdGw3aFo2cnRncUVZTXpZVk11SjJGMnIxSFNVMXpTQXZ3cGRZUDZyUkdGUkpFZmRBOW1tM1dLZk5MU2M1Y2xqejBYL1RYeTB2VmxBVjk1bDlxY2ZGelBtcmtOSXN0OUZaU3dwdkI0OUx5QVZrZTA0RlFQUHdMZ1ZINGdwaGlKSDNqdlo3SStKNWxTOFZBZ01CQUFHalVEQk9NQjBHQTFVZERnUVdCQlRUeVA2Q2M1SGxCSjUrdWNWQ3dHYzVvZ0tOR3pBZkJnTlZIU01FR0RBV2dCVFR5UDZDYzVIbEJKNSt1Y1ZDd0djNW9nS05HekFNQmdOVkhSTUVCVEFEQVFIL01BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQXZNUzRFUWVQL2lwVjRqT0c1bE82L3RZQ2IvaUplQWR1T25SaGtKazBEYlgzMjlsRExaaFRUTC94L3cvOW11Q1ZjdkxyekVwNlBOK1ZXZnc1RTVGV3RaTjB5aEd0UDlSK3ZabnJWK29jMnpHRCtubzEveVNGT2UzRWlKQ081ZGVoeEtqWUVtQlJ2NXNVL0xaRktacG96S04vQk1FYTZDcUx1eGJ6Yjd5a3hWcjdFVkZYd2x0UHh6RTlUbUw5T0FDTk55RjVlSkhXTVJNbGxhclV2a2NYbGg0cHV4NGtzOWU2elY5RFFCeTJ6ZHM5ZjFJM3F4ZzBlWDZKbkdyWGkvWmlDVCtsSmdWZTNaRlhpZWppTEFpS0IwNHNYVzN0aTBMVzNseDEzWTFZbFE0L3RscGdUZ2ZJSnhLVjZueVBpTG9LMG55d2JNZCt2cEFpckR0Mk9jK2hrPC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L2RzOktleUluZm8+PC9kczpTaWduYXR1cmU+PHNhbWw6U3ViamVjdD48c2FtbDpOYW1lSUQgU1BOYW1lUXVhbGlmaWVyPSJodHRwOi8vbG9jYWxob3N0OjgwODAvc2FtbDIvc2VydmljZS1wcm92aWRlci1tZXRhZGF0YS9vbmUiIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6MS4xOm5hbWVpZC1mb3JtYXQ6ZW1haWxBZGRyZXNzIj50ZXN0dXNlckBzcHJpbmcuc2VjdXJpdHkuc2FtbDwvc2FtbDpOYW1lSUQ+PHNhbWw6U3ViamVjdENvbmZpcm1hdGlvbiBNZXRob2Q9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpjbTpiZWFyZXIiPjxzYW1sOlN1YmplY3RDb25maXJtYXRpb25EYXRhIE5vdE9uT3JBZnRlcj0iMjA1Mi0wOS0yOFQwMjo1MDowNFoiIFJlY2lwaWVudD0iaHR0cDovL2xvY2FsaG9zdDo4MDgwL2xvZ2luL3NhbWwyL3Nzby9vbmUiIEluUmVzcG9uc2VUbz0iQVJRYzk4ZjIwMC1kY2Y3LTRkZjQtYTUyMi0wNzIwNjIwOGIwN2QiLz48L3NhbWw6U3ViamVjdENvbmZpcm1hdGlvbj48L3NhbWw6U3ViamVjdD48c2FtbDpDb25kaXRpb25zIE5vdEJlZm9yZT0iMjAyMS0wMS0yMFQwMTowMjo1NFoiIE5vdE9uT3JBZnRlcj0iMjA1Mi0wOS0yOFQwMjo1MDowNFoiPjxzYW1sOkF1ZGllbmNlUmVzdHJpY3Rpb24+PHNhbWw6QXVkaWVuY2U+aHR0cDovL2xvY2FsaG9zdDo4MDgwL3NhbWwyL3NlcnZpY2UtcHJvdmlkZXItbWV0YWRhdGEvb25lPC9zYW1sOkF1ZGllbmNlPjwvc2FtbDpBdWRpZW5jZVJlc3RyaWN0aW9uPjwvc2FtbDpDb25kaXRpb25zPjxzYW1sOkF1dGhuU3RhdGVtZW50IEF1dGhuSW5zdGFudD0iMjAyMS0wMS0yMFQwMDo0ODoyOVoiIFNlc3Npb25Ob3RPbk9yQWZ0ZXI9IjIwMjEtMDEtMjBUMDg6NDg6MjlaIiBTZXNzaW9uSW5kZXg9Il9lN2ExYTllNDk1YmZlMjI2NjQ5ZThkY2MzN2UxNDE1NDQ5NTIxNWQ2ZWIiPjxzYW1sOkF1dGhuQ29udGV4dD48c2FtbDpBdXRobkNvbnRleHRDbGFzc1JlZj51cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZFByb3RlY3RlZFRyYW5zcG9ydDwvc2FtbDpBdXRobkNvbnRleHRDbGFzc1JlZj48L3NhbWw6QXV0aG5Db250ZXh0Pjwvc2FtbDpBdXRoblN0YXRlbWVudD48c2FtbDpBdHRyaWJ1dGVTdGF0ZW1lbnQ+PHNhbWw6QXR0cmlidXRlIE5hbWU9InVpZCIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDpiYXNpYyI+PHNhbWw6QXR0cmlidXRlVmFsdWUgeHNpOnR5cGU9InhzOnN0cmluZyI+dGVzdHVzZXJAc3ByaW5nLnNlY3VyaXR5LnNhbWw8L3NhbWw6QXR0cmlidXRlVmFsdWU+PC9zYW1sOkF0dHJpYnV0ZT48c2FtbDpBdHRyaWJ1dGUgTmFtZT0iZWR1UGVyc29uQWZmaWxpYXRpb24iIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6YmFzaWMiPjxzYW1sOkF0dHJpYnV0ZVZhbHVlIHhzaTp0eXBlPSJ4czpzdHJpbmciPm1lbWJlcjwvc2FtbDpBdHRyaWJ1dGVWYWx1ZT48c2FtbDpBdHRyaWJ1dGVWYWx1ZSB4c2k6dHlwZT0ieHM6c3RyaW5nIj51c2VyPC9zYW1sOkF0dHJpYnV0ZVZhbHVlPjwvc2FtbDpBdHRyaWJ1dGU+PHNhbWw6QXR0cmlidXRlIE5hbWU9ImVtYWlsQWRkcmVzcyIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDpiYXNpYyI+PHNhbWw6QXR0cmlidXRlVmFsdWUgeHNpOnR5cGU9InhzOnN0cmluZyI+dGVzdHVzZXJAc3ByaW5nLnNlY3VyaXR5LnNhbWw8L3NhbWw6QXR0cmlidXRlVmFsdWU+PC9zYW1sOkF0dHJpYnV0ZT48L3NhbWw6QXR0cmlidXRlU3RhdGVtZW50Pjwvc2FtbDpBc3NlcnRpb24+PC9zYW1scDpSZXNwb25zZT4=";

	static final Map<String, List<Object>> USER_ATTRIBUTES = new LinkedHashMap<>();

	static {
		USER_ATTRIBUTES.put("uid", Arrays.asList("testuser@spring.security.saml"));
		USER_ATTRIBUTES.put("eduPersonAffiliation", Arrays.asList("member", "user"));
		USER_ATTRIBUTES.put("emailAddress", Arrays.asList("testuser@spring.security.saml"));
	}

	@Autowired
	MockMvc mvc;

	@Autowired
	WebClient webClient;

	@BeforeEach
	void setup() {
		this.webClient.getCookieManager().clearCookies();
	}

	@Test
	void indexWhenSamlResponseThenShowsUserInformation() throws Exception {
		HttpSession session = this.mvc.perform(get("http://localhost:8080/")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost:8080/login")).andReturn().getRequest()
				.getSession();

		this.mvc.perform(post("http://localhost:8080/login/saml2/sso/one").param("SAMLResponse", SIGNED_RESPONSE)
				.session((MockHttpSession) session)).andExpect(redirectedUrl("http://localhost:8080/"));

		this.mvc.perform(get("http://localhost:8080/").session((MockHttpSession) session))
				.andExpect(model().attribute("emailAddress", "testuser@spring.security.saml"))
				.andExpect(model().attribute("userAttributes", USER_ATTRIBUTES));
	}

	@Test
	void authenticationAttemptWhenValidThenShowsUserEmailAddress() throws Exception {
		HtmlPage relyingParty = performLogin();
		assertThat(relyingParty.asNormalizedText()).contains("You're email address is testuser@spring.security.saml");
	}

	@Test
	void logoutWhenRelyingPartyInitiatedLogoutThenLoginPageWithLogoutParam() throws Exception {
		HtmlPage relyingParty = performLogin();
		HtmlElement rpLogoutButton = relyingParty.getHtmlElementById("rp_logout_button");
		HtmlPage loginPage = rpLogoutButton.click();
		assertThat(loginPage.getUrl().getFile()).isEqualTo("/login?logout");
	}

	@Test
	void logoutWhenAssertingPartyInitiatedLogoutThenLoginPageWithLogoutParam() throws Exception {
		HtmlPage relyingParty = performLogin();
		HtmlElement apLogoutButton = relyingParty.getHtmlElementById("ap_logout_button");
		HtmlPage loginPage = apLogoutButton.click();
		assertThat(loginPage.getUrl().getFile()).isEqualTo("/login?logout");
	}

	private HtmlPage performLogin() throws IOException {
		HtmlPage login = this.webClient.getPage("/");
		HtmlPage assertingParty = login.getAnchorByHref("/saml2/authenticate/one").click();
		HtmlForm form = assertingParty.getFormByName("f");
		HtmlInput username = form.getInputByName("username");
		HtmlInput password = form.getInputByName("password");
		HtmlSubmitInput submit = assertingParty.getHtmlElementById("submit_button");
		username.setValueAttribute("user");
		password.setValueAttribute("password");
		return submit.click();
	}

}