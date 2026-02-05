# Business Associate Agreement (BAA) Template

## For AWS S3 and Other Service Providers

**IMPORTANT:** This is a template. Customize for your specific vendors and have legal counsel review before signing.

---

## BUSINESS ASSOCIATE AGREEMENT

**Effective Date:** [DATE]

**Covered Entity:** [YOUR HOSPITAL/ORGANIZATION NAME]

**Business Associate:** [VENDOR NAME, e.g., Amazon Web Services]

---

### 1. DEFINITIONS

**Covered Entity:** [Your organization name], a healthcare provider subject to HIPAA.

**Business Associate:** [Vendor name], a service provider that handles Protected Health Information (PHI) on behalf of the Covered Entity.

**Protected Health Information (PHI):** Individually identifiable health information transmitted or maintained in any form or medium.

---

### 2. PERMITTED USES AND DISCLOSURES

The Business Associate may use or disclose PHI only:
- To perform services for the Covered Entity as specified in the service agreement
- As required by law
- As permitted by this Agreement

---

### 3. OBLIGATIONS OF BUSINESS ASSOCIATE

The Business Associate agrees to:

**a) Safeguards:**
- Implement administrative, physical, and technical safeguards to protect PHI
- Use encryption for PHI in transit and at rest
- Maintain secure access controls

**b) Reporting:**
- Report any security incident or breach to Covered Entity within 24 hours
- Report any unauthorized use or disclosure of PHI
- Provide breach notification as required by HIPAA

**c) Access:**
- Allow Covered Entity to access PHI for patient requests
- Allow Covered Entity to amend PHI
- Allow Covered Entity to delete PHI when requested

**d) Compliance:**
- Comply with HIPAA Security Rule
- Comply with HIPAA Privacy Rule
- Maintain audit logs of PHI access

---

### 4. OBLIGATIONS OF COVERED ENTITY

The Covered Entity agrees to:
- Notify Business Associate of any limitations in patient authorization
- Notify Business Associate of any changes in patient authorization
- Notify Business Associate of any restrictions on use/disclosure of PHI

---

### 5. TERM AND TERMINATION

**Term:** This Agreement remains in effect until terminated.

**Termination:**
- Either party may terminate for material breach
- Upon termination, Business Associate must return or destroy all PHI
- If return/destruction is infeasible, Business Associate must continue to protect PHI

---

### 6. BREACH NOTIFICATION

Business Associate must notify Covered Entity of any breach within 24 hours of discovery, including:
- Nature of breach
- PHI involved
- Individuals affected
- Steps taken to mitigate

---

### 7. DATA RETURN AND DESTRUCTION

Upon termination:
- Business Associate must return all PHI to Covered Entity
- If return is infeasible, Business Associate must destroy PHI
- Business Associate must certify in writing that PHI has been returned or destroyed

---

### 8. AUDIT AND INSPECTION

Business Associate must:
- Maintain audit logs of PHI access
- Allow Covered Entity to inspect security measures
- Provide audit logs upon request

---

### 9. INDEMNIFICATION

Business Associate agrees to indemnify Covered Entity for:
- Breaches caused by Business Associate
- Costs of breach notification
- Regulatory fines and penalties

---

### 10. SIGNATURES

**Covered Entity:**
_________________________
[Name]
[Title]
[Date]

**Business Associate:**
_________________________
[Name]
[Title]
[Date]

---

## Notes for Implementation

1. **AWS S3:** AWS provides HIPAA-compliant BAAs. Sign their BAA through AWS Support.

2. **Other Vendors:** Customize this template for each vendor.

3. **Legal Review:** Have legal counsel review before signing.

4. **Storage:** Keep signed BAAs on file for audits.

5. **Renewal:** Review and renew BAAs annually.

---

## Vendor-Specific Notes

### AWS S3
- AWS provides their own BAA
- Sign through AWS Support
- Ensure S3 bucket encryption is enabled
- Use IAM roles with minimal permissions

### Database Hosting Provider
- Customize template
- Ensure database encryption
- Ensure SSL/TLS connections

### Email Service Provider
- Customize template
- Ensure email encryption
- Ensure secure transmission
